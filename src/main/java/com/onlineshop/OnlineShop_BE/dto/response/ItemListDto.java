package com.onlineshop.OnlineShop_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemListDto {
      private int itemId;
      private String itemName;
      private String itemCode;
      private int stock;
      private int price;
      private int isAvailable;
      private LocalDateTime lastReStock;
}
