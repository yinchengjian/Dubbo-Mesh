/*
 * RpcResponseDecoder.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author xinba
 */
public class RpcResponseDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        try {
            do {
                final int savedReaderIndex = in.readerIndex();
                Object msg = null;
                try {
                    msg = decode2(in);
                } catch (final Exception e) {
                    System.err.println("decode error.");
                    throw e;
                }
                if (msg == DubboRpcResponseDecoder.DecodeResult.NEED_MORE_INPUT) {
                    in.readerIndex(savedReaderIndex);
                    break;
                }

                out.add(msg);
            } while (in.isReadable());
        } finally {
            if (in.isReadable()) {
                in.discardReadBytes();
            }
        }


    }

    private Object decode2(final ByteBuf byteBuf) {

        final int readable = byteBuf.readableBytes();

        if (readable < 8) {
            return DubboRpcResponseDecoder.DecodeResult.NEED_MORE_INPUT;
        }
        final byte[] requestIdBytes = new byte[8];
        byteBuf.readBytes(requestIdBytes);
        if (readable < 12) {
            return DubboRpcResponseDecoder.DecodeResult.NEED_MORE_INPUT;
        }
        final long requestId = Bytes.bytes2long(requestIdBytes, 0);
        final byte[] lenBytes = new byte[4];
        byteBuf.readBytes(lenBytes);
        final int len = Bytes.bytes2int(lenBytes, 0);
        if (readable < 12 + len) {
            return DubboRpcResponseDecoder.DecodeResult.NEED_MORE_INPUT;
        }
        final byte[] data = new byte[len];
        byteBuf.readBytes(data);

        final RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setBytes(data);
        return response;
    }

}
