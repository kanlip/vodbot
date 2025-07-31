package com.example.demo.video.repository;

import com.example.demo.video.entity.VideoEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VideoRepositoryCustomImpl implements VideoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<VideoEntity> searchVideosWithMultipleCriteria(
        ObjectId companyId,
        Instant startDate,
        Instant endDate,
        ObjectId orderId,
        String platformOrderId,
        String sku,
        Pageable pageable
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Company ownership constraint - always required
        criteriaList.add(Criteria.where("companyId").is(companyId));

        // Add date range criteria if provided
        if (startDate != null && endDate != null) {
            criteriaList.add(
                Criteria.where("recordedAt").gte(startDate).lte(endDate)
            );
        } else if (startDate != null) {
            criteriaList.add(Criteria.where("recordedAt").gte(startDate));
        } else if (endDate != null) {
            criteriaList.add(Criteria.where("recordedAt").lte(endDate));
        }

        // Add order ID criteria if provided
        if (orderId != null) {
            criteriaList.add(Criteria.where("orderId").is(orderId));
        }

        // Add platform order ID criteria if provided
        if (platformOrderId != null && !platformOrderId.isEmpty()) {
            criteriaList.add(
                Criteria.where("platformOrderId").is(platformOrderId)
            );
        }

        // Add SKU criteria if provided
        if (sku != null && !sku.isEmpty()) {
            criteriaList.add(Criteria.where("itemScans.sku").is(sku));
        }

        // Combine all criteria with AND
        Criteria finalCriteria = new Criteria().andOperator(
            criteriaList.toArray(new Criteria[0])
        );

        // Create query with criteria
        Query query = new Query(finalCriteria);

        // Get total count for pagination
        long total = mongoTemplate.count(query, VideoEntity.class);

        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<VideoEntity> videos = mongoTemplate.find(query, VideoEntity.class);

        return new PageImpl<>(videos, pageable, total);
    }
}
