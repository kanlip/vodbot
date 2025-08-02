package com.example.demo.video.repository;

import com.example.demo.video.entity.VideoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VideoRepository extends MongoRepository<VideoEntity, String> {
    // You can add custom query methods here if needed
    Optional<VideoEntity> findByPlatformOrderId(String platformOrderId);
}
