package com.example.demo.order;

import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.internal.Platform;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderMongoRepository extends IOrderRepository, MongoRepository<OrderEntity, ObjectId> {
    Optional<OrderEntity> findByPlatformOrderIdAndPlatform(String id, Platform platform);
}
