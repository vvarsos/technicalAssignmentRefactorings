package com.example.shop.service;

import com.example.shop.model.CreateUserRequest;
import com.example.shop.model.User;
import com.example.shop.store.InMemoryStore;
import java.time.Instant;
import java.util.Locale;

public class UserService {
  private final InMemoryStore store = InMemoryStore.getInstance();

  public User createUser(CreateUserRequest req) {
    String id = req.getEmail().toLowerCase(Locale.US);
    User existing = store.getUser(id);
    if (existing != null) {
      existing.setName(req.getName());
      store.touchUser(id);
      return existing;
    }
    User user = new User(id, req.getEmail(), req.getName(), Instant.now());
    store.saveUser(user);
    return user;
  }

  public User getUser(String id) {
    return store.getUser(id);
  }
}
