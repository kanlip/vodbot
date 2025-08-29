package com.example.demo.order.adapter.out.persistence;

import com.example.demo.order.domain.Order;
import org.mapstruct.Mapper;

import java.util.UUID;
import java.util.stream.Collectors;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(OrderMapper.class);
    Order toDomain(OrderEntity entity);
    OrderEntity toEntity(Order order);
}
//public class OrderMapper {
//    public static Order toDomain(OrderEntity entity) {
//        if (entity == null) return null;
//        Order order = new Order();
//        order.setId(entity.getId() != null ? entity.getId().toString() : null);
//        order.setSellerId(entity.getSellerId());
//        order.setPlatformOrderId(entity.getPlatformOrderId());
//        order.setOrderDate(entity.getOrderDate());
//        order.setPlatform(entity.getPlatform());
//        order.setStatus(entity.getStatus());
//        order.setCreatedAt(entity.getCreatedAt());
//        order.setUpdatedAt(entity.getUpdatedAt());
//        if (entity.getPackages() != null) {
//            order.setPackageIds(entity.getPackages().stream()
//                    .map(p -> p.getId() != null ? p.getId().toString() : null)
//                    .collect(Collectors.toList()));
//        }
//        return order;
//    }
//
//    public static OrderEntity toEntity(Order order) {
//        if (order == null) return null;
//        OrderEntity entity = new OrderEntity();
//        if (order.getId() != null) entity.setId(UUID.fromString(order.getId()));
//        entity.setSellerId(order.getSellerId());
//        entity.setPlatformOrderId(order.getPlatformOrderId());
//        entity.setOrderDate(order.getOrderDate());
//        entity.setPlatform(order.getPlatform());
//        entity.setStatus(order.getStatus());
//        entity.setCreatedAt(order.getCreatedAt());
//        entity.setUpdatedAt(order.getUpdatedAt());
//        // packages handled separately
//        return entity;
//    }
//}
