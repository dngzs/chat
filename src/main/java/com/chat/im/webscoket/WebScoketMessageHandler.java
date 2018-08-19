package com.chat.im.webscoket;

import com.alibaba.fastjson.JSONObject;
import com.chat.im.proto.ChatCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebScoketMessageHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        TextWebSocketFrame frame =  (TextWebSocketFrame)object;
        String text = frame.text();
        JSONObject jsonObject = JSONObject.parseObject(text);
        Integer code = jsonObject.getInteger("code");
        if(code == ChatCode.MESS_CODE){
            UserInfo userInfo = UserInfoManager.getUserInfo(ctx.channel());
            if (userInfo != null && userInfo.isAuth()) {
                JSONObject json = JSONObject.parseObject(frame.text());
                // 广播返回用户发送的消息文本
                UserInfoManager.broadcastMessage(userInfo.getUserId(),userInfo.getNick(),json.getString("msg"));
            }
        }
    }
}
