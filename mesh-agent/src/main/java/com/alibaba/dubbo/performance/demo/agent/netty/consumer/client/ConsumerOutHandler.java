/*
 * ConsumerOutHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.netty.consumer.server.ConsumerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ConsumerOutHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final Logger logger = LoggerFactory.getLogger(ConsumerOutHandler.class);

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RpcResponse msg) throws Exception {

        final Channel serverChannel = ConsumerHandler.channels.get().remove(msg.getRequestId());
        if (serverChannel == null) {
            return;
        }
        serverChannel.writeAndFlush(msg).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("ConsumerServer---consumer data send successfully");
            } else {
                future.cause().printStackTrace();
            }
        });
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
