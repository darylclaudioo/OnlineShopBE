package com.onlineshop.OnlineShop_BE.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshop.OnlineShop_BE.dto.request.CreateItemRequest;
import com.onlineshop.OnlineShop_BE.dto.request.UpdateItemRequest;
import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.service.ItemService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/item")
public class ItemController {

   @Autowired
   private ItemService itemService;

   @PostMapping("/create")
   public MessageResponse create(@Valid @RequestBody CreateItemRequest request) {
      return itemService.createItem(request);
   }

   @GetMapping("/get-by-id")
   public ResponseEntity<Object> getById(@RequestParam int itemId) {
      return ResponseEntity.ok(itemService.getItemById(itemId));
   }

   @PutMapping(path={"/update"},consumes={MediaType.APPLICATION_JSON_VALUE,MediaType.MULTIPART_FORM_DATA_VALUE},produces={MediaType.APPLICATION_JSON_VALUE})
   public ResponseEntity<MessageResponse> update(@Valid @RequestPart("request") UpdateItemRequest request) {
      MessageResponse response = itemService.updateItem(request);
      return new ResponseEntity<>(response, HttpStatus.OK);
   }

   @PutMapping("/delete")
   public MessageResponse delete(@RequestParam int itemId) {
      return itemService.deleteItem(itemId);
   }

   @GetMapping("/get-all")
   public ResponseEntity<Object> getAll(
      @PageableDefault(page=0,size=8,sort="itemId",direction=Direction.ASC) Pageable pageable
   ) {
      return itemService.getAllItem(pageable);
   }
}
