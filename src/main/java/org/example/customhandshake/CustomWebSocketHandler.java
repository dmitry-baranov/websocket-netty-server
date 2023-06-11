package org.example.customhandshake;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.example.Utils;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/*
Обработчик сообщений WebSocket с кастомной реализацией рукопожатий
 */
public class CustomWebSocketHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = Logger.getLogger(CustomWebSocketHandler.class.getName());
    private static final Set<String> ipAddresses = new HashSet<>();
    private WebSocketServerHandshaker handShaker;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /*
    Обработчик соединения websocket
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().toString();
        if (!ipAddresses.add(ip)) {
            throw new RuntimeException("Number of connections exceeded for ip:" + ip);
        }
        logger.info("New connection ip:" + ip);
        super.channelActive(ctx);
    }

    /*
    Обработчик отсоединиея websocket
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().toString();
        ipAddresses.remove(ip);
        logger.info("Connection close ip:" + ip);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            //Запрос (http upgrade) на переход для взаимодействия по WebSocket
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            //Получение сообщений от клиента по WebSocket
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //разного рода фильтры перед обработкой сообщения
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException("Supported only TextWebSocketFrame");
        }

        ctx.channel().write(new TextWebSocketFrame(Utils.generateUniqueBigInteger()));
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        //создание handShaker
        if (handShaker == null) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws:localhost:websocket", null, false);
            handShaker = wsFactory.newHandshaker(req);
        }
        //происходит рукопожатие
        handShaker.handshake(ctx.channel(), req);
    }
}
