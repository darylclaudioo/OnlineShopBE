package com.onlineshop.OnlineShop_BE.dto.response;

import lombok.Data;

@Data
public class OrderListDto {
   private int orderId;
   private String orderCode;
   private String orderDate;
   private CustomerListDto customer;
   private ItemListDto item;
   private int quantity;
   private int totalPrice;
}