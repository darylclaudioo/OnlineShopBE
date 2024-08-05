package com.onlineshop.OnlineShop_BE.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateItemRequest {
      private String itemName;
      private int stock;
      private int price;
      private int isAvailable;
      private LocalDateTime lastReStock;
}
