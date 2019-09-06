/*
 * ProviderHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.server;

import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.netty.provider.client.ProviderClient;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ProviderHandler extends SimpleChannelInboundHandler<Object> {

    private final Logger logger = LoggerFactory.getLogger(ProviderHandler.class);

    private ChannelFuture clientChannelFuture;

    private final LoadBalance loadBalance;

    public ProviderHandler(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final ProviderClient providerClient = new ProviderClient(ctx.channel());
        final Endpoint endpoint = new Endpoint("127.0.0.1", 20889, 1);
        providerClient.connect(endpoint);
        clientChannelFuture = providerClient.getChannelFuture();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {

        clientChannelFuture.addListener(futures -> {
            if (futures.isSuccess()) {
                clientChannelFuture.channel().writeAndFlush(msg).addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("consumer data sended successfully---ProviderHandler");
                    } else {
                        future.cause().printStackTrace();
                    }
                });
            } else {
                logger.error("连接provider失败！！！");
                futures.cause().printStackTrace();
            }
        });
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
