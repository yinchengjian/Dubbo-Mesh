package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.loadbalance.PriorityLoadBalance;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Random;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private RpcClient rpcClient = new RpcClient(registry);
    private List<Endpoint> endpoints = null;
    private LoadBalance loadBalance = new PriorityLoadBalance();
    private Object lock = new Object();
    private AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();


    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") final String interfaceName,
                         @RequestParam("method") final String method,
                         @RequestParam("parameterTypesString") final String parameterTypesString,
                         @RequestParam("parameter") final String parameter) throws Exception {
        final String type = System.getProperty("type");   // 获取type参数
        if ("consumer".equals(type)) {
            return consumer(interfaceName, method, parameterTypesString, parameter);
        } else if ("provider".equals(type)) {
            return provider(interfaceName, method, parameterTypesString, parameter);
        } else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }

    public byte[] provider(final String interfaceName, final String method, final String parameterTypesString, final String parameter) throws Exception {

        final Object result = rpcClient.invoke(interfaceName, method, parameterTypesString, parameter);
        return (byte[]) result;
    }

    public DeferredResult<ResponseEntity> consumer(final String interfaceName, final String method, final String parameterTypesString, final String parameter) throws Exception {

        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    loadBalance.initEndpoints(endpoints);
                }
            }
        }

        // 简单的负载均衡，随机取一个
        Endpoint endpoint = loadBalance.select();

        final String url = "http://" + endpoint.getHost() + ":" + endpoint.getPort();

        final Request request = Dsl.post(url).addFormParam("interface", interfaceName)
                .addFormParam("method", method)
                .addFormParam("parameterTypesString", parameterTypesString)
                .addFormParam("parameter", parameter)
                .build();

        final DeferredResult<ResponseEntity> result = new DeferredResult();
        final ListenableFuture<Response> responseFuture = this.asyncHttpClient.executeRequest(request);
        final Runnable callBack = () -> {
            try {
                final String value = responseFuture.get().getResponseBody().trim();
                result.setResult(new ResponseEntity(value, HttpStatus.OK));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        };
        responseFuture.addListener(callBack, null);
        return result;

    }
}
