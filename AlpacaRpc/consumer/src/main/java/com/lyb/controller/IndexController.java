package com.lyb.controller;

import com.lyb.api.pojo.User;
import com.lyb.api.service.HelloService;
import com.lyb.api.service.UserService;
import com.lyb.rpc.annotation.ConsumerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
public class IndexController {
    @ConsumerService
    private HelloService helloService;
    @ConsumerService
    private UserService userService;

    @GetMapping("/testHello")
    public String testHello() throws InterruptedException {
        StringBuilder builder = new StringBuilder("HelloService 的 index() 方法测试").append("<br>");
        new Thread(new TestHello("Mr.Alpaca", helloService, builder)).start();
        TimeUnit.SECONDS.sleep(5);
        return builder.toString();
    }

    @GetMapping("/testUser")
    public String testUser() throws InterruptedException {
        StringBuilder builder = new StringBuilder();
        userService.register(new User(1, String.valueOf(1), new Date(), new Date()));
        userService.getAll().forEach(user -> builder.append(user).append("<br>"));
        return builder.toString();
    }

    @GetMapping("/testAll")
    public String testAll() throws InterruptedException {
        StringBuilder builder = new StringBuilder("所有方法测试").append("<br>");
        new Thread(new TestHello("Mr.Alpaca", helloService, builder)).start();
        new Thread(new TestUser("Mr.Alpaca", userService, builder)).start();
        TimeUnit.SECONDS.sleep(5);
        return builder.toString();
    }
}

class TestHello implements Runnable {
    private final String name;
    private final HelloService service;
    private final StringBuilder builder;

    public TestHello(String name, HelloService service, StringBuilder builder) {
        this.name = name;
        this.service = service;
        this.builder = builder;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            builder.append(i).append(" ").append(service.index(name)).append(" 耗时: ").append(System.currentTimeMillis() - start).append("<br>");
        }
    }
}

class TestUser implements Runnable {
    private final String name;
    private final UserService service;
    private final StringBuilder builder;

    public TestUser(String name, UserService service, StringBuilder builder) {
        this.name = name;
        this.service = service;
        this.builder = builder;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            long start = System.currentTimeMillis();
            User user = new User(i, String.valueOf(i), new Date(), new Date());
            service.register(user);
        }
        service.getAll().forEach(user -> builder.append(user).append("<br>"));
    }
}
