package com.lyb.provider.service.impl;

import com.lyb.api.service.HelloService;
import com.lyb.rpc.annotation.ProviderService;

@ProviderService
public class HelloServiceImpl implements HelloService {
    @Override
    public String index(String name) {
        return "I am One, Hello " + name;
    }
}
