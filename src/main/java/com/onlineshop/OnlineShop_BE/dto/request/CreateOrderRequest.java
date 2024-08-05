package com.onlineshop.OnlineShop_BE.dto.request;

import lombok.Data;

@Data
public class CreateOrderRequest {
   private int customerId;
   private int itemId;
   private int quantity;
}
