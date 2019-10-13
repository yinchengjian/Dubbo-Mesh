/*
 * ConsumerServer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.PriorityLoadBalance;
import com.alibaba.dubbo.performance.demo.agent.netty.consumer.client.ConsumerClient;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author xinba
 */
public class ConsumerServer {
    private final Logger logger = LoggerFactory.getLogger(ConsumerServer.class);

    private ServerBootstrap serverBootstrap;

    private final EventLoopGroup bootLoopGroup = new NioEventLoopGroup(1);

    private final EventLoopGroup workerLoopGroup = new NioEventLoopGroup(4);

    private final IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private final LoadBalance loadBalance = new PriorityLoadBalance();


    public void start(final int port) {
        try {
            final List<Endpoint> endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
            loadBalance.initEndpoints(endpoints);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        initClient(workerLoopGroup);
        try {
            serverBootstrap = new ServerBootstrap()
                    .group(bootLoopGroup, workerLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ConsumerInitializer(loadBalance));
            logger.info("consumer agent start successfully");
            final ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            bootLoopGroup.shutdownGracefully();
            workerLoopGroup.shutdownGracefully();
        }
    }

    public void initClient(final EventLoopGroup eventLoopGroup) {
        for (final EventExecutor eventExecutor : eventLoopGroup) {
            if (eventExecutor instanceof EventLoop) {
                final ConsumerClient consumerClient = new ConsumerClient((EventLoop) eventExecutor);
                final Endpoint endpoint = loadBalance.select();
                consumerClient.connect(endpoint);
                ConsumerClient.map.put(eventExecutor.toString(), consumerClient.getChannel());
            }
        }
    }
}
