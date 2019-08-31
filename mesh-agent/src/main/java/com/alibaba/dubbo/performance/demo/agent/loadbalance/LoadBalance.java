package com.alibaba.dubbo.performance.demo.agent.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.List;

/**
 * @Author yinchengjian
 * @Date 2019/8/31 10:47
 */
public interface LoadBalance {

    Endpoint select();

    void initEndpoints(List<Endpoint> endpoints);
}
