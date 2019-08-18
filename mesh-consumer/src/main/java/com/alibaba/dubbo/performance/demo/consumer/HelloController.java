//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alibaba.dubbo.performance.demo.consumer;

import java.util.Random;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.RandomStringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class HelloController {
    private AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
    private ResponseEntity ok;
    private ResponseEntity error;
    Random r;

    public HelloController() {
        this.ok = new ResponseEntity("OK", HttpStatus.OK);
        this.error = new ResponseEntity("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        this.r = new Random(1L);
    }

    @RequestMapping({"/invoke"})
    public DeferredResult<ResponseEntity> invoke() {
        String str = RandomStringUtils.random(this.r.nextInt(1024), true, true);
        String url = "http://127.0.0.1:20000";
        DeferredResult<ResponseEntity> result = new DeferredResult();
        Request request = ((RequestBuilder)((RequestBuilder)((RequestBuilder)((RequestBuilder)Dsl.post(url).addFormParam("interface", "com.alibaba.dubbo.performance.demo.provider.IHelloService")).addFormParam("method", "hash")).addFormParam("parameterTypesString", "Ljava/lang/String;")).addFormParam("parameter", str)).build();
        ListenableFuture<Response> responseFuture = this.asyncHttpClient.executeRequest(request);
        Runnable callback = () -> {
            try {
                String value = ((Response)responseFuture.get()).getResponseBody();
                if (String.valueOf(str.hashCode()).equals(value)) {
                    result.setResult(this.ok);
                } else {
                    result.setResult(this.error);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        };
        responseFuture.addListener(callback, (Executor)null);
        return result;
    }
}
