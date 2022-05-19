package com.lyb.provider2.service.impl;

import com.lyb.api.service.HelloService;
import com.lyb.rpc.annotation.ProviderService;
import org.springframework.stereotype.Component;

@ProviderService
@Component
public class HelloServiceImpl implements HelloService {
    @Override
    public String index(String name) {
        return "I am Two, Hello " + name;
    }
}
