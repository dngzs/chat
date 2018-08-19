package com.chat.im.webscoket;

import com.alibaba.fastjson.JSONObject;
import com.chat.im.proto.ChatCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 认证
 */
public class WebScoketAuthMessageHandler extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebScoketAuthMessageHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        TextWebSocketFrame frame =  (TextWebSocketFrame)object;
        String text = frame.text();
        JSONObject jsonObject = JSONObject.parseObject(text);
        Integer code = jsonObject.getInteger("code");
        if(ChatCode.AUTH_CODE == code){
            String token = jsonObject.getString("token");
            //todo 验证token凭证，如果token凭证出错，或者用户没有登陆，则关闭连接
            if(token != null && StringUtils.isBlank(token)){
                LOGGER.error("auth fail，token = {}",token);
                ctx.channel().writeAndFlush("认证失败,连接关闭");
                //关闭连接
                ctx.channel().close();
            }
            //添加认证标志
            boolean auth = UserInfoManager.saveUserInfo(ctx.channel());
            //发送上线信息
            sendOnlineMessage(ctx, auth);
            //如果成功，移除掉认证
            finalHandler(ctx, frame);
        }
    }

    /**
     * 后续处理
     * @param ctx
     * @param msg
     */
    private void finalHandler(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(msg);
    }

    /**
     * 发送上线信息
     * @param ctx
     * @param auth
     */
    private void sendOnlineMessage(ChannelHandlerContext ctx, boolean auth) {
        UserInfoManager.sendInfo(ctx.channel(),ChatCode.SYS_AUTH_STATE,auth);
        UserInfoManager.sendInfo(ctx.channel(),ChatCode.SYS_AUTH_STATE,"上线了");
    }

}
