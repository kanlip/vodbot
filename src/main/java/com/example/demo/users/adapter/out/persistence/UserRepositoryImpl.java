package com.example.demo.users.adapter.out.persistence;

import com.example.demo.users.domain.User;
import com.example.demo.users.port.out.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SpringDataUsersRepository springDataUsersRepository;
    private final EntityManager entityManager;


    @Override
    public User findById(UUID id) {
        Optional<UsersEntity> entity = springDataUsersRepository.findById(id);
        return entity.map(UserMapper.INSTANCE::toDomain).orElse(null);
    }

    @Override
    public List<User> findAll() {
        return springDataUsersRepository.findAll().stream().map(UserMapper.INSTANCE::toDomain).collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        UsersEntity entity = UserMapper.INSTANCE.toEntity(user);
        UsersEntity saved = springDataUsersRepository.save(entity);
        return UserMapper.INSTANCE.toDomain(saved);
    }

    @Override
    public void disableUser(UUID id) {
        Optional<UsersEntity> entityOpt = springDataUsersRepository.findById(id);
        entityOpt.ifPresent(entity -> {
            entity.setStatus("disabled");
            springDataUsersRepository.save(entity);
        });
    }
}
