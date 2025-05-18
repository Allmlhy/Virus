package com.dhr.maven.virus_backend.service;

import com.dhr.maven.virus_backend.pojo.UserInfo;

import java.util.List;

public interface UserService {
    UserInfo createUser(UserInfo user);
    UserInfo updateUser(Long id, UserInfo user);
    void deleteUser(Long id);
    UserInfo getUser(Long id);
    List<UserInfo> getAllUsers();
}
