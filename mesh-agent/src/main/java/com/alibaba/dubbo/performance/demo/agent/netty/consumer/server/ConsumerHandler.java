/*
 * ConsumerHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.netty.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.netty.consumer.client.ConsumerClient;
import com.alibaba.dubbo.performance.demo.agent.util.RequestParser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xinba
 */
public class ConsumerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    public static final FastThreadLocal<LongObjectHashMap<Channel>> channels = new FastThreadLocal<LongObjectHashMap<Channel>>() {
        @Override
        protected LongObjectHashMap<Channel> initialValue() throws Exception {
            return new LongObjectHashMap<>();
        }
    };

    public static AtomicLong requestIdGenerator = new AtomicLong(0);


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
    public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest msg) throws Exception {
        final Request request = transform(msg);

        channels.get().put(request.getId(), ctx.channel());
        //provider agent 返回数据
        final ChannelFuture clientChannelFuture = ConsumerClient.map.get(ctx.executor().toString());
        clientChannelFuture.addListener(future -> {
            if (future.isSuccess()) {
                clientChannelFuture.channel().writeAndFlush(request).addListener(futures -> {
                    if (futures.isSuccess()) {
                        logger.info("ConsumerClient---consumer data send successfully");
                    } else {
                        futures.cause().printStackTrace();
                    }
                });
            } else {
                logger.error("连接provider-agent失败！！！");
                future.cause().printStackTrace();
            }
        });
    }

    private Request transform(final FullHttpRequest msg) throws Exception {
        final Map<String, String> params = RequestParser.fastParse(msg);
        final RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(params.get("method"));
        invocation.setAttachment("path", params.get("interfaceName"));
        invocation.setParameterTypes(params.get("parameterTypesString"));    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(params.get("parameter"), writer);
        invocation.setArguments(out.toByteArray());

        final Request request = new Request();
        request.setId(requestIdGenerator.incrementAndGet());
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        return request;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
