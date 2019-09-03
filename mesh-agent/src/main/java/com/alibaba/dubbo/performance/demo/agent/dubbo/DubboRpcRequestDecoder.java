/*
 * DubboRpcRequestDecoder.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * @author xinba
 */
public class DubboRpcRequestDecoder extends ByteToMessageDecoder {

    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;
    protected static final byte FLAG_EVENT = (byte) 0x20;


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

                out.add(msg);
            } while (in.isReadable());
        } finally {
            if (in.isReadable()) {
                in.discardReadBytes();
            }
        }
    }

    private Object decode2(final ByteBuf byteBuf) {
        final Request request = new Request();
        final byte[] header = new byte[HEADER_LENGTH];
        byteBuf.readBytes(header);
        final int len = Bytes.bytes2int(header, 12);
        final byte[] data = new byte[len];
        byteBuf.readBytes(data);

        final byte[] subArray = Arrays.copyOfRange(data, 51, data.length - 4);
        final String s = new String(subArray);
        System.err.println(s);


        //todo decode bytes to object
        final RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName("hash");
        invocation.setParameterTypes("Ljava/lang/String;");
        invocation.setAttachment("path", "com.alibaba.dubbo.performance.demo.provider.IHelloService");
        invocation.setArguments(subArray);
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        return request;

    }

}
