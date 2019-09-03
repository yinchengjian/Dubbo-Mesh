/*
 * Test.java
 * Copyright 2019 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.alibaba.dubbo.performance.demo.agent.consumer;

/**
 * @author xinba
 */
public class Test {
    public static void main(final String[] args) {
        final String s = "51";
        System.out.println(s.getBytes());
        for (final byte b : s.getBytes()) {
            System.out.println(b);
        }
    }
}
