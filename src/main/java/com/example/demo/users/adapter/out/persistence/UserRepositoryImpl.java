package com.example.demo.users.adapter.out.persistence;

import com.example.demo.users.domain.User;
import com.example.demo.users.port.out.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SpringDataUsersRepository springDataUsersRepository;
    private final SpringDataOrganizationsRepository organizationsRepository;
    private final EntityManager entityManager;


    @Override
    public Optional<User> findById(UUID id) {
        Optional<UsersEntity> entity = springDataUsersRepository.findById(id);
        return entity.map(UserMapper.INSTANCE::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<UsersEntity> entity = springDataUsersRepository.findByEmail(email);
        return entity.map(UserMapper.INSTANCE::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUsersRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return springDataUsersRepository.findAll().stream().map(UserMapper.INSTANCE::toDomain).collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        // For new users, we need to set up the organization relationship
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }
        user.setUpdatedAt(Instant.now());

        // Create entity and set organization
        UsersEntity entity = new UsersEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRoles(user.getRoles());
        entity.setDisplayName(user.getDisplayName());
        entity.setIsSupervisor(user.isSupervisor());
        entity.setSupervisorPinHash(user.getSupervisorPinHash());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setStatus(user.getStatus());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        // Set the organization relationship
        if (user.getOrgId() != null) {
            OrganizationsEntity orgEntity = organizationsRepository.findById(user.getOrgId())
                    .orElseThrow(() -> new RuntimeException("Organization not found: " + user.getOrgId()));
            entity.setOrganizationsEntity(orgEntity);
        }

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
