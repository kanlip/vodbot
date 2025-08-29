package com.example.demo.users.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataUsersRepository extends JpaRepository<UsersEntity, UUID> {
}
