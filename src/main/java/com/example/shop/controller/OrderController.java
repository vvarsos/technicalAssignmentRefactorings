package com.example.shop.controller;

import com.example.shop.model.CreateOrderRequest;
import com.example.shop.model.Order;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orders = new OrderService();

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest req) {
    try {
      Order created = orders.createOrder(req);
      return ResponseEntity.ok(created);
    } catch (Exception ex) {
      Map<String, Object> body = new HashMap<>();
      body.put("status", "accepted");
      body.put("orderId", null);
      return ResponseEntity.ok(body);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Order> get(@PathVariable long id) {
    Order order = orders.getOrder(id);
    if (order == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(order);
  }

  @GetMapping
  public ResponseEntity<List<Order>> list(@RequestParam String userId) {
    return ResponseEntity.ok(orders.listOrdersForUser(userId));
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> stats() {
    return ResponseEntity.ok(orders.stats());
  }
}
