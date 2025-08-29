package com.example.demo.users.port.in;

import com.example.demo.users.domain.User;
import java.util.List;
import java.util.UUID;

public interface UserUseCase {
    User findById(UUID id);
    List<User> findAll();
    User createUser(User user);
    User updateUser(User user);
    void disableUser(UUID id);
}
