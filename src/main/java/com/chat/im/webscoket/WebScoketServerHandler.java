package com.chat.im.webscoket;

import com.alibaba.fastjson.JSONObject;
import com.chat.im.proto.ChatCode;
import com.chat.im.util.Constants;
import com.chat.im.util.NettyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebScoketServerHandler extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebScoketServerHandler.class);

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {

        if (message instanceof FullHttpRequest) {
            //处理http请求
            handHttpRequest(channelHandlerContext, (FullHttpRequest) message);
        }
        if(message instanceof WebSocketFrame){
            handlerWebSocketFrame(channelHandlerContext, (WebSocketFrame) message);
        }
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 开始处理webSocket请求
        closeRequest(ctx, frame);
        //ping消息
        if(frame instanceof PingWebSocketFrame){
            LOGGER.info("ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        }
        // Pong消息
        if (frame instanceof PongWebSocketFrame) {
            LOGGER.info("pong message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //不支持非文本消息
        if(!(frame instanceof TextWebSocketFrame)){
            //防止刷新浏览器造成的关闭
            closeRequest(ctx,frame);
            throw new UnsupportedOperationException("no support binary message");
        }

        //文本消息处理
        handlerWebSocketMessage(ctx, frame);

    }

    private void closeRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
            UserInfoManager.removeChannel(ctx.channel());
        }
    }

    /**
     * 处理消息流转
     * @param ctx
     * @param frame
     */
    private void handlerWebSocketMessage(ChannelHandlerContext ctx, WebSocketFrame frame) {
        String message = ((TextWebSocketFrame) frame).text();
        JSONObject jsonObject = JSONObject.parseObject(message);
        Integer code = jsonObject.getInteger("code");
        switch (code){
            case ChatCode.PING_CODE:
            case ChatCode.PONG_CODE:
                //如果有心跳，则记录最新的心跳时间
                UserInfoManager.updateUserTime(ctx.channel(),System.currentTimeMillis());
                LOGGER.info("receive pong message, address: {}",NettyUtil.parseChannelRemoteAddr(ctx.channel()));
                return;
            case ChatCode.AUTH_CODE:
                //交给认证的channel
                ctx.fireChannelRead(frame.retain());
                break;
            case ChatCode.MESS_CODE:
                //交给后续的消息channel去做
                ctx.fireChannelRead(frame.retain());
        }
    }


    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        //如果是一次不成功的请求，则返回错误信息
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        }
        //否则
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(Constants.WEBSOCKET_URL,null,false);
        this.handshaker = factory.newHandshaker(req);
        //如果等于空，则不支持
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{
            //开始握手
            handshaker.handshake(ctx.channel(),req);
            //保存握手信息
            UserInfoManager.addChannel(ctx.channel());
        }


    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        if (res.status().code() != HttpResponseStatus.OK.code()) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(byteBuf);
            byteBuf.release();
        }
        //是非持久连接，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if(!HttpUtil.isKeepAlive(req) || res.status().code() != HttpResponseStatus.OK.code()){
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("fail",cause);
        if(ctx.channel() != null){
            ctx.close();
        }
    }

}
