package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.pojo.UserInfo;
import com.dhr.maven.virus_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserInfo createUser(@RequestBody UserInfo user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public UserInfo getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping
    public List<UserInfo> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public UserInfo updateUser(@PathVariable Long id, @RequestBody UserInfo user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
