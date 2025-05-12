package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
}
