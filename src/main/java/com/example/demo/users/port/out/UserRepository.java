package com.example.demo.users.port.out;

import com.example.demo.users.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User save(User user);
    void disableUser(UUID id);
    boolean existsByEmail(String email);
}
