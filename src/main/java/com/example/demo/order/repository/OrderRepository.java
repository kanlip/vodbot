package com.example.demo.order.repository;

import com.example.demo.order.entity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderRepository extends CrudRepository<Order, ObjectId> {
    Optional<Order> findByPlatformOrderId(String id);

}
