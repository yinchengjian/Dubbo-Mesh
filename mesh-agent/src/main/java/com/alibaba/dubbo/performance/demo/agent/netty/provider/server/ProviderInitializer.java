/*
 * ProviderInitializer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.DubboRpcRequestDecoder;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xinba
 */
public class ProviderInitializer extends ChannelInitializer<SocketChannel> {

    private final LoadBalance loadBalance;


    public ProviderInitializer(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        //rpcRequest入站要解密
        pipeline.addLast(new DubboRpcRequestDecoder());
        pipeline.addLast(new ProviderHandler(loadBalance));
    }
}
