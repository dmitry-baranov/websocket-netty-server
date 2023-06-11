package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.example.customhandshake.CustomWebSocketHandler;
import org.example.withoutcustomhandshake.DefaultWebSocketHandler;


/*
--------
написать на Java сервер который является websocket сервером
при запросе на сервер, должен возвращаться ответ в виде JSON - рандомное число BigInteger
сервер не должен позволяет с одного IP адреса устанавливать более 1 соединения
сервер должен гарантировать что число всегда будет уникальное, появившись один раз - больше не должно появляться

не использовать фреймворки типа Spring, RxJava и пр

описать в readme как запустить и использовать сервер

unit тесты и не docker - не обязательно, если в коде будут комментарии
---------
Возможные вопросы:
В чем различие протоколов http и websocket
http синхронизированный протокол в отором клиент отправляет запрос а сервер ему отвечает
websocket протокол full-duplex в котором как клиент так и сервер может оправлять сообщения независимо друг от друга
можно грубо сравнить с топиками кафки (producer and consumer)
но важно отметить что у http тоже недавно появилось
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        if (args[0] != null) {
            new Server(8000).run(args[0]);
        } else {
            throw new IllegalArgumentException("requiredParam is missing");
        }
    }

    private static final int MAX_WEBSOCKET_HANDSHAKE_SIZE = 65536;

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run(String param) throws InterruptedException {
        //создаем 2 NioEventLoopGroup для большей производительности,
        //bossGroup отвечает за прием входящих соединений и передачу их на workerGroup
        //workerGroup обрабатывает входящие данные
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //настройка сервера
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    //будет прослушивать канал NioServerSocketChannel реализация netty
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //отвечает за кодирование и декодирование http запросов
                            ch.pipeline().addLast(new HttpServerCodec());
                            //отвечает за объединение нескольких http запросов в один FullHttpRequest(Response)
                            //при разбиание запроса на несколько TCP сообщений мы собираем все в один целый http запрос
                            //этим гарантируем обработку целого http запроса
                            ch.pipeline().addLast(new HttpObjectAggregator(MAX_WEBSOCKET_HANDSHAKE_SIZE));
                            if (param.equals("custom-handshake")) {
                                ch.pipeline().addLast(new CustomWebSocketHandler());
                            } else if (param.equals("default-handshake")) {
                                //отвечает за обработку рукопожатий
                                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
                                //обработчик websocket сообщений от клиента
                                ch.pipeline().addLast(new DefaultWebSocketHandler());
                            } else {
                                throw new UnsupportedOperationException(param + " not supported");
                            }
                        }
                    });
            // привязывает сервер и начинает слушать указанный порт
            ChannelFuture f = bootstrap.bind("localhost", port).sync();
            // приложение будет ждать пока канал сервера не закроется
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
