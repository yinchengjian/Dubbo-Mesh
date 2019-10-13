/*
 * ProviderServer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.server;

import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.PriorityLoadBalance;
import com.alibaba.dubbo.performance.demo.agent.netty.provider.client.ProviderClient;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author xinba
 */
public class ProviderServer {

    private ServerBootstrap serverBootstrap;

    private final EventLoopGroup bootEventLoop = new NioEventLoopGroup(1);

    private final EventLoopGroup workerEventLoop = new NioEventLoopGroup(4);

    private final LoadBalance loadBalance = new PriorityLoadBalance();

    public void start() {
        final EtcdRegistry etcdRegistry = new EtcdRegistry(System.getProperty("etcd.url"));
        try {
            final List<Endpoint> endpoints = etcdRegistry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
            loadBalance.initEndpoints(endpoints);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        initClient(workerEventLoop);
        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bootEventLoop, workerEventLoop)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(Integer.valueOf(System.getProperty("server.port"))))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ProviderInitializer(loadBalance));
            final Channel channel = serverBootstrap.bind().sync().channel();
            channel.closeFuture().sync();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            bootEventLoop.shutdownGracefully();
            workerEventLoop.shutdownGracefully();
        }
    }

    public void initClient(final EventLoopGroup eventLoopGroup) {
        for (final EventExecutor eventExecutor : eventLoopGroup) {
            if (eventExecutor instanceof EventLoop) {
                final ProviderClient providerClient = new ProviderClient((EventLoop) eventExecutor);
                final Endpoint endpoint = new Endpoint("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")), 1);
                providerClient.connect(endpoint);
                ProviderClient.map.put(eventExecutor.toString(), providerClient.getChannel());
            }
        }
    }

}
