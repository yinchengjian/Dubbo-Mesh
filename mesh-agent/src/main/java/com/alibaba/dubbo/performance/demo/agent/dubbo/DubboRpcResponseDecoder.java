/*
 * DubboRpcResponseDecoder.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

public class DubboRpcResponseDecoder extends ByteToMessageDecoder {
    // header length.
    protected static final int HEADER_LENGTH = 16;

    protected static final byte FLAG_EVENT = (byte) 0x20;

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf byteBuf, final List<Object> list) {

        try {
            do {
                final int savedReaderIndex = byteBuf.readerIndex();
                final SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
                Object msg = null;
                try {
                    msg = decode2(byteBuf);
                } catch (final Exception e) {
                    System.err.println("decode error.");
                    throw e;
                }
                if (msg == DecodeResult.NEED_MORE_INPUT) {
                    byteBuf.readerIndex(savedReaderIndex);
                    break;
                }

                list.add(msg);
            } while (byteBuf.isReadable());
        } finally {
            if (byteBuf.isReadable()) {
                byteBuf.discardReadBytes();
            }
        }


        //list.add(decode2(byteBuf));
    }

    enum DecodeResult {
        NEED_MORE_INPUT, SKIP_INPUT
    }

    /**
     * Demo为简单起见，直接从特定字节位开始读取了的返回值，demo未做：
     * 1. 请求头判断
     * 2. 返回值类型判断
     *
     * @param byteBuf
     * @return
     */
    private Object decode2(final ByteBuf byteBuf) {

        final int savedReaderIndex = byteBuf.readerIndex();
        final int readable = byteBuf.readableBytes();

        if (readable < HEADER_LENGTH) {
            return DecodeResult.NEED_MORE_INPUT;
        }

        final byte[] header = new byte[HEADER_LENGTH];
        byteBuf.readBytes(header);
        final byte[] dataLen = Arrays.copyOfRange(header, 12, 16);
        final int len = Bytes.bytes2int(dataLen);
        final int tt = len + HEADER_LENGTH;
        if (readable < tt) {
            return DecodeResult.NEED_MORE_INPUT;
        }

        byteBuf.readerIndex(savedReaderIndex);
        final byte[] data = new byte[tt];
        byteBuf.readBytes(data);


        //byte[] data = new byte[byteBuf.readableBytes()];
        //byteBuf.readBytes(data);

        // HEADER_LENGTH + 1，忽略header & Response value type的读取，直接读取实际Return value
        // dubbo返回的body中，前后各有一个换行，去掉
        final byte[] subArray = Arrays.copyOfRange(data, HEADER_LENGTH + 3, data.length - 2);

        final String s = new String(subArray);
        System.err.println(s);

        final byte[] requestIdBytes = Arrays.copyOfRange(data, 4, 12);
        final long requestId = Bytes.bytes2long(requestIdBytes, 0);

        final RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setBytes(subArray);
        return response;
    }
}