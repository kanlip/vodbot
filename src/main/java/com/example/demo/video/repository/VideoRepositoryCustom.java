package com.example.demo.video.repository;

import com.example.demo.video.entity.VideoEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface VideoRepositoryCustom {
    Page<VideoEntity> searchVideosWithMultipleCriteria(
            ObjectId companyId,
            Instant startDate,
            Instant endDate,
            ObjectId orderId,
            String platformOrderId,
            String sku,
            Pageable pageable
    );
}