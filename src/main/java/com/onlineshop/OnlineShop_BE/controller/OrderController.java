package com.onlineshop.OnlineShop_BE.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshop.OnlineShop_BE.dto.request.CreateOrderRequest;
import com.onlineshop.OnlineShop_BE.dto.request.UpdateOrderRequest;
import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/order")
public class OrderController {

   @Autowired
   private OrderService orderService;

   @PostMapping("/create")
   public MessageResponse create(@Valid @RequestBody CreateOrderRequest request) {
      return orderService.createOrder(request);
   }

   @GetMapping("/get-by-id")
   public ResponseEntity<Object> getById(@RequestParam int orderId) {
      return ResponseEntity.ok(orderService.getOrderById(orderId));
   }

   @PutMapping(path = { "/update" },consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},produces = {MediaType.APPLICATION_JSON_VALUE})
   public ResponseEntity<MessageResponse> update(@Valid @RequestPart("request") UpdateOrderRequest request) {
      MessageResponse response = orderService.updateOrder(request);
      return new ResponseEntity<>(response, HttpStatus.OK);

   }

   @PutMapping("/delete")
   public MessageResponse delete(@RequestParam int orderId) {
      return orderService.deleteOrder(orderId);
   }

   @GetMapping("/get-all")
   public ResponseEntity<Object> getAll(
      @PageableDefault(page = 0,size = 8,sort = "orderId",direction = Direction.ASC) Pageable pageable
   ) {
      return orderService.getAllOrder(pageable);
   }
}
