package com.onlineshop.OnlineShop_BE.dto.request;

import lombok.Data;

@Data
public class UpdateOrderRequest {
      private int orderId;
      private int customerId;
      private int itemId;
      private int quantity;
}
