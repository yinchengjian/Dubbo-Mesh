/*
 * ConsumerOutInitializer.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.DubboRpcRequestEncoder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcResponseDecoder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcResponseToFullHttpResponseEecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author xinba
 */
public class ConsumerOutInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        //出站
        pipeline.addLast(new DubboRpcRequestEncoder());
        //进站
        pipeline.addLast(new RpcResponseDecoder());
        pipeline.addLast(new RpcResponseToFullHttpResponseEecoder());
        pipeline.addLast(new ConsumerOutHandler());
    }
}