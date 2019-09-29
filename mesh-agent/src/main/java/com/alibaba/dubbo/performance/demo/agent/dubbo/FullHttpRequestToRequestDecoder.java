/*
 * FullHttpRequestToRequestDecoder.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.util.RequestParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xinba
 */
public class FullHttpRequestToRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {


    public static AtomicLong requestIdGenerator = new AtomicLong(0);

    @Override
    protected void decode(final ChannelHandlerContext ctx, final FullHttpRequest msg, final List<Object> out) throws Exception {
        final Request request = transform(msg);
        out.add(request);
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
}
