package com.example.demo.order.port.in;

import com.example.demo.order.domain.Order;
import java.util.List;

public interface OrderUseCase {
    Order findById(String id);
    List<Order> findAll();
    Order createOrder(Order order);
    Order updateOrder(Order order);
    void cancelOrder(String id);
}
