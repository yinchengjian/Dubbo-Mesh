/*
 * ProviderServer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.provider;

import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author xinba
 */
public class ProviderServer {

    private ServerBootstrap serverBootstrap;

    private final EventLoopGroup bootEventLoop = new NioEventLoopGroup(1);

    private final EventLoopGroup workerEventLoop = new NioEventLoopGroup(4);


    public void start() {
        new EtcdRegistry(System.getProperty("etcd.url"));
        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bootEventLoop, workerEventLoop)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(30000))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .childHandler(new ProviderInitializer());
            final Channel channel = serverBootstrap.bind().sync().channel();
            channel.closeFuture().sync();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            bootEventLoop.shutdownGracefully();
            workerEventLoop.shutdownGracefully();
        }

    }

}
