/*
 * ConsumerClient.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.client;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xinba
 */
public class ConsumerClient {

    public static final Map<String, ChannelFuture> map = new HashMap<>();

    private final EventLoop eventLoop;

    private ChannelFuture channelFuture;

    public ConsumerClient(final EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void connect(final Endpoint endpoint) {
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoop)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerOutInitializer());
        this.channelFuture = bootstrap.connect(endpoint.getHost(), endpoint.getPort());
    }
}
