package com.example.demo.users.adapter.in.web;

import com.example.demo.users.domain.User;
import com.example.demo.users.port.in.UserUseCase;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userUseCase.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable UUID id) {
        return userUseCase.findById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userUseCase.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        return userUseCase.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public void disableUser(@PathVariable UUID id) {
        userUseCase.disableUser(id);
    }
}

