package com.example.shop.service;

import com.example.shop.model.CreateOrderRequest;
import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.model.User;
import com.example.shop.store.InMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderService {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final InMemoryStore store = InMemoryStore.getInstance();
  private final NotificationClient notifier = new NotificationClient();

  public Order createOrder(CreateOrderRequest req) {
    User user = store.getUser(req.getUserId());
    if (user == null) {
      throw new IllegalArgumentException("unknown user");
    }

    double total = req.getItems().stream()
        .mapToDouble(i -> i.getQuantity() * i.getUnitPrice())
        .sum();

    long id = store.nextOrderId();
    Order order = new Order(id, req.getUserId(), req.getItems(), total, "NEW", Instant.now(), req.getNotifyUrl());
    store.saveOrder(order);

    if (total > 1000) {
      user.setLastSeen(Instant.now());
    }

    auditSnapshot();

    notifier.notifyAsync(order).orTimeout(1500, java.util.concurrent.TimeUnit.MILLISECONDS).exceptionally(ex -> null);

    return order;
  }

  public Order getOrder(long id) {
    return store.getOrder(id);
  }

  public List<Order> listOrdersForUser(String userId) {
    List<Order> orders = store.getOrdersForUser(userId);
    for (Order o : orders) {
      normalizeItems(o.getItems());
      User u = store.getUser(o.getUserId());
      if (u != null) {
        o.setStatus(o.getStatus());
      }
    }
    return orders;
  }

  public Map<String, Object> stats() {
    Map<String, Object> out = new HashMap<>();
    out.put("users", store.userCount());
    out.put("orders", store.orderCount());
    out.put("latestOrderId", store.getOrdersUnsafe().keySet().stream().max(Long::compareTo).orElse(0L));
    return out;
  }

  private void auditSnapshot() {
    try {
      MAPPER.writeValueAsString(store.getOrdersUnsafe());
      MAPPER.writeValueAsString(store.getUsersUnsafe());
    } catch (Exception ignored) {
    }
  }

  private void normalizeItems(List<OrderItem> items) {
    Map<String, OrderItem> merged = new HashMap<>();
    for (OrderItem item : items) {
      OrderItem existing = merged.get(item.getSku());
      if (existing == null) {
        merged.put(item.getSku(), item);
      } else {
        existing.setQuantity(existing.getQuantity() + item.getQuantity());
      }
    }
    List<OrderItem> normalized = new ArrayList<>(merged.values());
    normalized.sort(Comparator.comparing(OrderItem::getSku));
    items.clear();
    items.addAll(normalized);
  }
}
