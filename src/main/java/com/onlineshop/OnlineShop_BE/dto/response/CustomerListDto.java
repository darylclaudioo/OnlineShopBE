package com.onlineshop.OnlineShop_BE.dto.response;

import lombok.Data;
@Data
public class CustomerListDto {
      private int customerId;
      private String customerName;
      private String customerAddress;
      private String customerCode;
      private String customerPhone;
      private int isActive;
      private String lastOrderDate;
      private String pic;
}
