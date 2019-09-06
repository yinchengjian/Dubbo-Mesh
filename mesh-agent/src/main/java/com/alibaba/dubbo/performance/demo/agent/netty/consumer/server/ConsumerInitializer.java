/*
 * ConsumerInitializer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @author xinba
 */
public class ConsumerInitializer extends ChannelInitializer<SocketChannel> {

    private final LoadBalance loadBalance;

    public ConsumerInitializer(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }


    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
        pipeline.addLast(new ConsumerHandler(loadBalance));
    }
}
