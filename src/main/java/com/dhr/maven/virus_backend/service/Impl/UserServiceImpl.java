package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.pojo.UserInfo;
import com.dhr.maven.virus_backend.repository.UserRepository;
import com.dhr.maven.virus_backend.service.UserService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserInfo createUser(UserInfo user) {
        return userRepository.save(user);
    }

    @Override
    public UserInfo updateUser(Long id, UserInfo user) {
        Optional<UserInfo> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            UserInfo u = existing.get();
            u.setUsername(user.getUsername());
            return userRepository.save(u);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserInfo getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<UserInfo> getAllUsers() {
        return userRepository.findAll();
    }
}
