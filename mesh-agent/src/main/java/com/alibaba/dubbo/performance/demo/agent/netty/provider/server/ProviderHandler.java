/*
 * ProviderHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.provider.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.netty.provider.client.ProviderClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ProviderHandler extends SimpleChannelInboundHandler<Request> {

    private final Logger logger = LoggerFactory.getLogger(ProviderHandler.class);

    public static final FastThreadLocal<Channel> channels = new FastThreadLocal<>();

    private final LoadBalance loadBalance;

    public ProviderHandler(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        channels.set(ctx.channel());
//        final ProviderClient providerClient = new ProviderClient(ctx.channel());
//        final Endpoint endpoint = new Endpoint("127.0.0.1", 20889, 1);
//        providerClient.connect(endpoint);
//        clientChannelFuture = providerClient.getChannelFuture();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Request msg) throws Exception {
        final Channel channel = ProviderClient.map.get(ctx.executor().toString());
        channel.writeAndFlush(msg).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("ProviderClient---provider data send successfully");
            } else {
                logger.info("ProviderClient---provider data send failure");
                future.cause().printStackTrace();
            }
        });
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
