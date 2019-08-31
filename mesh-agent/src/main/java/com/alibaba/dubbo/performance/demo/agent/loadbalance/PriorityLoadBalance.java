package com.alibaba.dubbo.performance.demo.agent.loadbalance;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author yinchengjian
 * @Date 2019/8/31 10:48
 */
public class PriorityLoadBalance implements LoadBalance {

    private List<Endpoint> endpoints = new ArrayList<>();
    private int weight;
    AtomicInteger cursor = new AtomicInteger(0);
    @Override
    public Endpoint select() {
        return endpoints.get(cursor.getAndAdd(1)%weight);
    }

    @Override
    public void initEndpoints(List<Endpoint> endpoints){
        for(Endpoint endpoint:endpoints){
            this.weight+=endpoint.getWeight();
            for(int i=0;i<endpoint.getWeight();i++){
                this.endpoints.add(endpoint);
            }
        }
        Collections.shuffle(this.endpoints);
    }
}
