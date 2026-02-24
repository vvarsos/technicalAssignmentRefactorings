package com.example.shop.model;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
  private long id;
  private String userId;
  private List<OrderItem> items;
  private double total;
  private String status;
  private Instant createdAt;
  private String notifyUrl;
}
