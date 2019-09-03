/*
 * ProviderOutInitializer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.provider;

import com.alibaba.dubbo.performance.demo.agent.dubbo.DubboRpcRequestEncoder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.DubboRpcResponseDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xinba
 */
public class ProviderOutInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel serverChannel;

    public ProviderOutInitializer(final Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DubboRpcRequestEncoder());
        pipeline.addLast(new DubboRpcResponseDecoder());
        pipeline.addLast(new ProviderOutHandler(serverChannel));
    }
}