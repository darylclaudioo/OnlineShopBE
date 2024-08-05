package com.onlineshop.OnlineShop_BE.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlineshop.OnlineShop_BE.model.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
   

}
