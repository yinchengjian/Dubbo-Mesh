package com.alibaba.dubbo.performance.demo.agent.dubbo.model;

public class RpcResponse {

    private long requestId;
    private byte[] bytes;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(final long requestId) {
        this.requestId = requestId;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(final byte[] bytes) {
        this.bytes = bytes;
    }
}
