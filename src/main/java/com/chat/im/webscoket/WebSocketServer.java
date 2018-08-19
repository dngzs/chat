package com.chat.im.webscoket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebSocket服务端
 *
 * @author  zhangbo
 *
 */
@Component
public class WebSocketServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();

    @PostConstruct
    public void init(){
        executorService.submit(()->{
            LOGGER.info("netty start...");
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebScoketServerInitializer());
            try {
                ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(8899)).sync();
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                LOGGER.error("netty webSocket init fail ",e);
                work.shutdownGracefully();
                boss.shutdownGracefully();
            }
        });
    }

    @PreDestroy
    public void destroy(){
        if(work != null){
            work.shutdownGracefully();
        }
        if(boss != null){
            boss.shutdownGracefully();
        }
        LOGGER.info("destroy webSocket successful");
    }

}
