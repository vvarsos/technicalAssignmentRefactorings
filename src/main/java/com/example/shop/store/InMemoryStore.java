package com.example.shop.store;

import com.example.shop.model.Order;
import com.example.shop.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryStore {
  private static final InMemoryStore INSTANCE = new InMemoryStore();

  private final Map<String, User> users = new HashMap<>();
  private final Map<Long, Order> orders = new HashMap<>();
  private final Map<String, List<Order>> ordersByUser = new HashMap<>();
  private final AtomicLong orderSeq = new AtomicLong(1000);

  public static InMemoryStore getInstance() {
    return INSTANCE;
  }

  public synchronized void saveUser(User user) {
    users.put(user.getId(), user);
  }

  public synchronized User getUser(String id) {
    return users.get(id);
  }

  public synchronized void saveOrder(Order order) {
    orders.put(order.getId(), order);
    List<Order> bucket = ordersByUser.computeIfAbsent(order.getUserId(), k -> new ArrayList<>());
    bucket.add(order);
  }

  public synchronized Order getOrder(long id) {
    return orders.get(id);
  }

  public synchronized List<Order> getOrdersForUser(String userId) {
    List<Order> list = ordersByUser.get(userId);
    if (list == null) {
      return Collections.emptyList();
    }
    return list;
  }

  public long nextOrderId() {
    return orderSeq.incrementAndGet();
  }

  public synchronized int userCount() {
    return users.size();
  }

  public synchronized int orderCount() {
    return orders.size();
  }

  public void touchUser(String id) {
    User u = users.get(id);
    if (u != null) {
      u.setLastSeen(Instant.now());
    }
  }

  public Map<Long, Order> getOrdersUnsafe() {
    return orders;
  }

  public Map<String, User> getUsersUnsafe() {
    return users;
  }
}
