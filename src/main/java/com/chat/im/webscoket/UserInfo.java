package com.chat.im.webscoket;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登陆信息认证
 */
public class UserInfo {
    private static AtomicInteger uidGener = new AtomicInteger(1000);

    /**
     * 是否认证
     */
    private boolean isAuth = false;
    /**
     * 登录时间
     */
    private long time = 0;
    /**
     * UID
     */
    private int userId;
    /**
     * 昵称
     */
    private String nick;
    /**
     * 地址
     */
    private String addr;
    /**
     * 通道
     */
    private Channel channel;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId() {
        this.userId = uidGener.incrementAndGet();
    }
}

