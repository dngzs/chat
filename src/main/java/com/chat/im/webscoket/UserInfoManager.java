package com.chat.im.webscoket;

import com.chat.im.util.ChatProto;
import com.chat.im.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserInfoManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserInfoManager.class);

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * 在线人数
     */
    private static AtomicInteger userCount = new AtomicInteger(0);

    /**
     * 保存用户握手信息
     */
    private static ConcurrentMap<Channel, UserInfo> userInfos = new ConcurrentHashMap<>();

    public static void addChannel(Channel channel){
        String remoteAddr = NettyUtil.parseChannelRemoteAddr(channel);
        if (!channel.isActive()) {
            LOGGER.error("channel is not active, address: {}", remoteAddr);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setAddr(remoteAddr);
        userInfo.setChannel(channel);
        userInfo.setTime(System.currentTimeMillis());
        userInfos.put(channel, userInfo);
    }

    public static void updateUserTime(Channel channel,long time) {
        UserInfo userInfo = userInfos.get(channel);
        userInfo.setTime(time);
    }

    /**
     * 获取用户信息
     * @param channel
     * @return
     */
    public static UserInfo getUserInfo(Channel channel) {
        return userInfos.get(channel);
    }

    /**
     * 广播普通消息
     *
     * @param message
     */
    public static void broadcastMessage(int uid, String nick, String message) {
        if (StringUtils.isNotBlank(message)) {
            try {
                lock.readLock().lock();
                Set<Channel> keySet = userInfos.keySet();
                for (Channel ch : keySet) {
                    UserInfo userInfo = userInfos.get(ch);
                    if (userInfo == null || !userInfo.isAuth()){
                        continue;
                    }
                    ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildMessProto(uid, nick, message)));
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }


    public static boolean saveUserInfo(Channel channel) {
        UserInfo userInfo = userInfos.get(channel);
        if (userInfo == null) {
            return false;
        }
        if (!channel.isActive()) {
            LOGGER.error("channel is not active, address: {}, nick: {}", userInfo.getAddr());
            return false;
        }
        // 增加一个认证用户
        userCount.incrementAndGet();
        userInfo.setAuth(true);
        userInfo.setUserId();
        userInfo.setTime(System.currentTimeMillis());
        return true;
    }


    /**
     * 发送系统消息
     *
     * @param code
     * @param mess
     */
    public static void sendInfo(Channel channel, int code, Object mess) {
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mess)));
    }

    public static void removeChannel(Channel channel) {
        userInfos.remove(channel);
    }
}
