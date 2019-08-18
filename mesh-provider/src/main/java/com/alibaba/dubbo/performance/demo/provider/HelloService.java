//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alibaba.dubbo.performance.demo.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloService implements IHelloService {
    private long count;
    private Logger logger = LoggerFactory.getLogger(HelloService.class);

    public HelloService() {
    }

    public int hash(String str) throws Exception {
        int hashCode = str.hashCode();
        this.logger.info(++this.count + "_" + hashCode);
        this.sleep(50L);
        return hashCode;
    }

    private void sleep(long duration) throws Exception {
        Thread.sleep(duration);
    }
}
