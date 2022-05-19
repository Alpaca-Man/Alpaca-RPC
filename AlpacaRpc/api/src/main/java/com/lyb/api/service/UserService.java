package com.lyb.api.service;

import com.lyb.api.pojo.User;

import java.util.List;

/**
 * 描述
 *
 * @author Mr.Alpaca
 * @version 1.0.0
 */
public interface UserService {
    void register(User user);
    User get(Integer id);
    List<User> getAll();
    boolean delete(Integer id);
}
