package com.onlineshop.OnlineShop_BE.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Order implements java.io.Serializable {
   private static final long serialVersionUID = -5894679636266655135L;

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "order_id", updatable = false, nullable = false)
   private int orderId;

   @Column(name = "order_code")
   private String orderCode;

   @Column(name = "order_date", nullable = false, updatable = false)
   private Date orderDate;

   @Column(name = "total_price")
   private int totalPrice;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "customer_id", nullable = false)
   private Customer customer;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "items_id", nullable = false)
   private Item item;

   @Column(name = "quantity")
   private int quantity;
}
