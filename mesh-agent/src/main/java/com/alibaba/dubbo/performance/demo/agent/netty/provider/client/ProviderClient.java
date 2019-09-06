/*
 * ProviderClient.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.client;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author xinba
 */
public class ProviderClient {

    private final Channel serverChannel;

    private ChannelFuture channelFuture;

    public ProviderClient(final Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void connect(final Endpoint endpoint) {
        final Bootstrap bootstrap = new Bootstrap();
        channelFuture = bootstrap.group(serverChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ProviderOutInitializer(serverChannel))
                .connect(endpoint.getHost(), endpoint.getPort());
    }
}
