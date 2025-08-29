package com.example.demo.order.adapter.out.persistence;

import com.example.demo.order.domain.Order;
import com.example.demo.order.port.out.OrderRepository;
import com.example.demo.shared.domain.Platform;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderSpringDataRepository springDataOrdersRepository;
    private final SessionFactory session;

    @Override
    public Order findById(UUID id) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return List.of();
    }

    @Override
    public Order save(Order order) {
        OrderEntity orderEntity = OrderMapper.INSTANCE.toEntity(order);
        OrderEntity saved = springDataOrdersRepository.save(orderEntity);
        return OrderMapper.INSTANCE.toDomain(saved);
    }

    @Override
    public Order update(Order order) {
        OrderEntity orderEntity = OrderMapper.INSTANCE.toEntity(order);
        OrderEntity updated = session.openSession().merge(orderEntity);
        return OrderMapper.INSTANCE.toDomain(updated);
    }

    @Override
    public OrderEntity findByPlatform(Platform platform, String platformOrderId, String sellerId) {
        return session
                .openSession()
                .createSelectionQuery("from OrderEntity where platform = :platform and platformOrderId = :platformOrderId and sellerId = :sellerId", OrderEntity.class)
                .setParameter("platform", platform)
                .setParameter("platformOrderId", platformOrderId)
                .setParameter("sellerId", sellerId)
                .getSingleResultOrNull();
    }

}
