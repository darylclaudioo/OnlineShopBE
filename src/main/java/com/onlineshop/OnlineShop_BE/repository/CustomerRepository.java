package com.onlineshop.OnlineShop_BE.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlineshop.OnlineShop_BE.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
   
}
