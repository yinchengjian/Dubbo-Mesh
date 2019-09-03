/*
 * ConsumerHandler.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.consumer;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.util.RequestParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
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

    private Channel clientChannel;

    private final LoadBalance loadBalance;

    public ConsumerHandler(final LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }


    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {


    }

    /**
     * 从consumer传递过来的数据
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final Request request = transform((FullHttpRequest) msg);
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ConsumerOutInitializer(ctx.channel()));
        //provider agent 返回数据
        final Endpoint endpoint = loadBalance.select();
        final ChannelFuture connectFuture = bootstrap.connect(endpoint.getHost(), endpoint.getPort());
        clientChannel = connectFuture.channel();
        connectFuture.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("成功连接到provider-agent");
                clientChannel.writeAndFlush(request).addListener(futures -> {
                    if (futures.isSuccess()) {
                        System.out.println("consumer data sended successfully");
                    } else {
                        futures.cause().printStackTrace();
                    }
                });
            } else {
                System.out.println("连接失败");
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
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
