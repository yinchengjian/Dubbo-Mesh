/*
 * DubboRpcResponseEncoder.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author xinba
 */
public class DubboRpcResponseEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) throws Exception {
        final RpcResponse response = (RpcResponse) msg;
        final long requestId = response.getRequestId();
        final byte[] requestIdBytes = new byte[8];
        Bytes.long2bytes(requestId, requestIdBytes, 0);
        out.writeBytes(requestIdBytes);
        final byte[] data = response.getBytes();
        final byte[] len = new byte[4];
        Bytes.int2bytes(data.length, len, 0);
        out.writeBytes(len);
        out.writeBytes(response.getBytes());
    }
}
