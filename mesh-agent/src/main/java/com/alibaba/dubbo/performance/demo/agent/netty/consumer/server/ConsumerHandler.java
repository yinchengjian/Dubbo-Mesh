/*
 * ConsumerHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.netty.consumer.client.ConsumerClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xinba
 */
public class ConsumerHandler extends SimpleChannelInboundHandler<Request> {

    private final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    public static final FastThreadLocal<LongObjectHashMap<Channel>> channels = new FastThreadLocal<LongObjectHashMap<Channel>>() {
        @Override
        protected LongObjectHashMap<Channel> initialValue() throws Exception {
            return new LongObjectHashMap<>();
        }
    };

    private final LoadBalance loadBalance;

    public ConsumerHandler(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        //todo 解决DUBBO Thread pool is EXHAUSTED!问题
//        final ConsumerClient consumerClient = new ConsumerClient(ctx.channel());
//        final Endpoint endpoint = new Endpoint("127.0.0.1", 30000, 1);
//        consumerClient.connect(endpoint);
//        clientChannelFuture = consumerClient.getChannelFuture();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Request msg) throws Exception {
        channels.get().put(msg.getId(), ctx.channel());
        logger.info("save serverChannel:{}", ctx.channel());
        //provider agent 返回数据
        final Channel clientChannel = ConsumerClient.map.get(ctx.executor().toString());
        logger.info("use clientChannel:{}", clientChannel.toString());
        clientChannel.writeAndFlush(msg).addListener(futures -> {
            if (futures.isSuccess()) {
                logger.info("ConsumerClient---consumer data send successfully");
            } else {
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
