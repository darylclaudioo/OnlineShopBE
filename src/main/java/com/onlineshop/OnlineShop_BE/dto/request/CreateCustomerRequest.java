package com.onlineshop.OnlineShop_BE.dto.request;

import lombok.Data;

@Data
public class CreateCustomerRequest {
   private String customerName;
   private String customerAddress;
   private String customerPhone;
   private int isActive;
   private String pic;
}
