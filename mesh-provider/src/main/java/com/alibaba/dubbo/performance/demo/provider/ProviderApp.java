//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alibaba.dubbo.performance.demo.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProviderApp {
    public ProviderApp() {
    }

    public static void main(String[] args) {
        SpringApplication.run(ProviderApp.class, args);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("do something...");
        }, 1000L, 5L, TimeUnit.SECONDS);
    }
}
