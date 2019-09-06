/*
 * ProviderOutHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ProviderOutHandler extends SimpleChannelInboundHandler<Object> {

    private final Logger logger = LoggerFactory.getLogger(ProviderOutHandler.class);

    private final Channel serverChannel;

    public ProviderOutHandler(final Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        logger.info("provider service response has reviced");
        final byte[] data = ((RpcResponse) msg).getBytes();
        //final byte[] subArray = Arrays.copyOfRange(data, 2, data.length - 1);
        final ByteBuf response = Unpooled.copiedBuffer(data);
        serverChannel.writeAndFlush(response).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("consumer data sended successfully---ProviderOutHandler");
            } else {
                future.cause().printStackTrace();
            }
        });
    }


}
