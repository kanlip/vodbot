package com.example.demo.order.entity;

import com.example.demo.common.Platform;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "orders")
@Data
@CompoundIndex(
    name = "sellerId_orderId_idx",
    def = "{'sellerId': 1, 'orderId': 1}",
    unique = true
)
public class OrderEntity {

    @Id
    ObjectId id;

    String sellerId;
    String platformOrderId;
    Instant orderDate;

    Platform platform;

    String status;

    Instant createdAt;
    Instant updatedAt;

    List<ObjectId> packageEntityList;
}
