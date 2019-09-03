/*
 * ProviderHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.provider;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ProviderHandler extends SimpleChannelInboundHandler<Object> {

    private final Logger logger = LoggerFactory.getLogger(ProviderHandler.class);

    private Channel clientChannel;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ProviderOutInitializer(ctx.channel()));
        final ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 20889);
        clientChannel = channelFuture.channel();
        channelFuture.addListener(futures -> {
            if (channelFuture.isSuccess()) {
                clientChannel.writeAndFlush(msg).addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("consumer data sended successfully---ProviderHandler");
                    } else {
                        future.cause().printStackTrace();
                    }
                });
            } else {
                futures.cause().printStackTrace();
            }
        });
        logger.info("provider client request has recived");
    }

}
