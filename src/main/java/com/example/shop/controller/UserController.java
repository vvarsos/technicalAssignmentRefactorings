package com.example.shop.controller;

import com.example.shop.model.CreateUserRequest;
import com.example.shop.model.User;
import com.example.shop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService users = new UserService();

  @PostMapping
  public ResponseEntity<User> create(@Valid @RequestBody CreateUserRequest req) {
    User created = users.createUser(req);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> get(@PathVariable String id) {
    User u = users.getUser(id);
    if (u == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(u);
  }
}
