package com.onlineshop.OnlineShop_BE.dto.request;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
      private int customerId;
      private String customerName;
      private String customerAddress;
      private String customerCode;
      private String customerPhone;
      private int isActive;
      private String pic;
}
