package com.example.shop.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
  @NotBlank
  private String sku;
  @Min(1)
  private int quantity;
  @Min(0)
  private double unitPrice;
}
