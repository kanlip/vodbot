package com.example.demo.users.adapter.out.persistence;

import com.example.demo.users.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(UserMapper.class);
    @Mapping(source = "entity.organizationsEntity.id", target = "orgId")
    User toDomain(UsersEntity entity);
    UsersEntity toEntity(User user);
}
