package org.example.withoutcustomhandshake;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.example.Utils;
import org.example.customhandshake.CustomWebSocketHandler;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/*
Обработчик сообщений WebSocket без реализации обработки переключения на WebSocket протокол
 */
public class DefaultWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger logger = Logger.getLogger(CustomWebSocketHandler.class.getName());
    private static final Set<String> ipAddresses = new HashSet<>();

    /*
    Обработчик полученного сообщения
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        //разного рода фильтры перед обработкой сообщения
        if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().close();
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException("Supported only TextWebSocketFrame");
        }
        ctx.channel().writeAndFlush(new TextWebSocketFrame(Utils.generateUniqueBigInteger()));
    }

    /*
    Обработчик соединения websocket
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().toString();
        if (!ipAddresses.add(ip)) {
            throw new RuntimeException("Number of connections exceeded");
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

    /*
    Обработчик исключений
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
