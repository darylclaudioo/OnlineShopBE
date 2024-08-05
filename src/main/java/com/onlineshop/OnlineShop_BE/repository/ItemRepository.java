package com.onlineshop.OnlineShop_BE.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlineshop.OnlineShop_BE.model.Item;

public interface ItemRepository extends JpaRepository<Item,Integer>{

}
