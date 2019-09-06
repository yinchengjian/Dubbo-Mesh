package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.netty.consumer.server.ConsumerServer;
import com.alibaba.dubbo.performance.demo.agent.netty.provider.server.ProviderServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。

    public static void main(final String[] args) {
        SpringApplication.run(AgentApp.class, args);
        final String type = System.getProperty("type");   // 获取type参数
        if ("provider".equals(type)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new ProviderServer().start();
                }
            }).start();
        }
        if ("consumer".equals(type)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new ConsumerServer().start(20000);
                }
            }).start();
        }
    }
}
