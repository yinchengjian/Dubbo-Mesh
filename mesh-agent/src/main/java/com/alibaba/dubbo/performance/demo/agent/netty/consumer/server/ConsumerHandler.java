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
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.util.RequestParser;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author xinba
 */
public class ConsumerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    private ChannelFuture clientChannelFuture;

    private final LoadBalance loadBalance;

    public ConsumerHandler(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final ConsumerClient consumerClient = new ConsumerClient(ctx.channel());
        final Endpoint endpoint = new Endpoint("127.0.0.1", 30000, 1);
        consumerClient.connect(endpoint);
        clientChannelFuture = consumerClient.getChannelFuture();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final Request request = transform((FullHttpRequest) msg);
        //provider agent 返回数据
        clientChannelFuture.addListener(future -> {
            if (future.isSuccess()) {
                clientChannelFuture.channel().writeAndFlush(request).addListener(futures -> {
                    if (futures.isSuccess()) {
                        logger.info("consumer data sended successfully");
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
