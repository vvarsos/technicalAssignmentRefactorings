package com.example.shop.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class CreateOrderRequest {
  @NotBlank
  private String userId;
  @NotEmpty
  @Valid
  private List<OrderItem> items;
  private String notifyUrl;
}
