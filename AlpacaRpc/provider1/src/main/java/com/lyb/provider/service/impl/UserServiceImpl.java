package com.lyb.provider.service.impl;
import com.lyb.api.pojo.User;
import com.lyb.api.service.UserService;
import com.lyb.rpc.annotation.ProviderService;

import java.util.ArrayList;
import java.util.List;

@ProviderService
public class UserServiceImpl implements UserService {
    private final List<User> userList = new ArrayList<>();
    @Override
    public void register(User user) {
        userList.add(user);
    }

    @Override
    public User get(Integer id) {
        return userList.get(id);
    }

    @Override
    public List<User> getAll() {
        return userList;
    }

    @Override
    public boolean delete(Integer id) {
        return userList.remove(id);
    }
}
