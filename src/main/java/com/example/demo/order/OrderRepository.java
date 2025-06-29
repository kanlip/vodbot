package com.example.demo.order;

import com.example.demo.order.internal.Order;
import com.example.demo.order.internal.Platform;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    Optional<Order> findByPlatformOrderIdAndPlatform(String id, Platform platform);

}
