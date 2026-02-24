package com.example.shop.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private String id;
  private String email;
  private String name;
  private Instant createdAt;
  private Instant lastSeen;

  public User(String id, String email, String name, Instant createdAt) {
    this(id, email, name, createdAt, null);
  }
}
