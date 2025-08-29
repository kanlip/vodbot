package com.example.demo.order.adapter.out.persistence;

import com.example.demo.shared.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderSpringDataRepository extends JpaRepository<OrderEntity, UUID> { // name kept for backward wiring
}
