/*
 * ProviderOutHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.netty.provider.server.ProviderHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ProviderOutHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final Logger logger = LoggerFactory.getLogger(ProviderOutHandler.class);

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RpcResponse msg) throws Exception {
        final Channel serverChannel = ProviderHandler.channels.get();
        logger.info("channel:{}", serverChannel.toString());
        serverChannel.writeAndFlush(msg).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("ProviderServer---provider data send successfully");
            } else {
                future.cause().printStackTrace();
            }
        });
    }


}
