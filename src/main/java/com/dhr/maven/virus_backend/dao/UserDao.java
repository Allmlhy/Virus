package com.dhr.maven.virus_backend.dao;

import com.dhr.maven.virus_backend.pojo.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<UserInfo, Long> {
    // 根据用户名查询用户
    UserInfo findByUsername(String username);
}
