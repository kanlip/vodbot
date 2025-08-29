package com.example.demo.users.port.out;

import com.example.demo.users.domain.User;
import java.util.List;
import java.util.UUID;

public interface UserRepository {
    User findById(UUID id);
    List<User> findAll();
    User save(User user);
    void disableUser(UUID id);
}
