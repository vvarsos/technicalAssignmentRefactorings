package com.example.shop.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
  @NotBlank
  @Email
  private String email;
  @NotBlank
  private String name;
}
