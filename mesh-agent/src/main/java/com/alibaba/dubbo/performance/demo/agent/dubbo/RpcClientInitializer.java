package com.alibaba.dubbo.performance.demo.agent.dubbo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new DubboRpcRequestEncoder());
        pipeline.addLast(new DubboRpcResponseDecoder());
        pipeline.addLast(new RpcClientHandler());
    }
}
