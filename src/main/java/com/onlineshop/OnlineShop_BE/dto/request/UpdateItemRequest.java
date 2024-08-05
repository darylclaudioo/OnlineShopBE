package com.onlineshop.OnlineShop_BE.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateItemRequest {
   private int itemId;
   private String itemName;
   private String itemCode;
   private int stock;
   private int price;
   private int isAvailable;
   private LocalDateTime lastReStock;
}
