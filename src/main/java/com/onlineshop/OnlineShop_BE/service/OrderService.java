package com.onlineshop.OnlineShop_BE.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onlineshop.OnlineShop_BE.dto.request.CreateOrderRequest;
import com.onlineshop.OnlineShop_BE.dto.request.UpdateOrderRequest;
import com.onlineshop.OnlineShop_BE.dto.response.CustomerListDto;
import com.onlineshop.OnlineShop_BE.dto.response.ItemListDto;
import com.onlineshop.OnlineShop_BE.dto.response.OrderListDto;
import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.dto.response.ResponseBodyDTO;
import com.onlineshop.OnlineShop_BE.model.Customer;
import com.onlineshop.OnlineShop_BE.model.Item;
import com.onlineshop.OnlineShop_BE.model.Order;
import com.onlineshop.OnlineShop_BE.repository.CustomerRepository;
import com.onlineshop.OnlineShop_BE.repository.ItemRepository;
import com.onlineshop.OnlineShop_BE.repository.OrderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

   @Autowired
   private CustomerRepository customerRepository;

   @Autowired
   private ItemRepository itemRepository;

   @Autowired
   private OrderRepository orderRepository;

   @Transactional
   public MessageResponse createOrder(CreateOrderRequest request) {
      log.info("request: " + request);
      try {
         Optional<Customer> optionalCustomer = customerRepository.findById(request.getCustomerId());

         if (!optionalCustomer.isPresent()) {
            return new MessageResponse("Customer not found", HttpStatus.NOT_FOUND.value(), "ERROR");
         }
         Optional<Item> optionalItem = itemRepository.findById(request.getItemId());
         log.info("optionalItem: " + optionalItem);

         if (!optionalItem.isPresent()) {
            return new MessageResponse("Item not found", HttpStatus.NOT_FOUND.value(), "ERROR");
         }
         int totalPrice = request.getQuantity() * itemRepository.findById(request.getItemId()).get().getPrice();

         Item item = itemRepository.findById(request.getItemId()).get();

         if (item.getStock() == 0) {
            item.setIsAvailable(0);
            itemRepository.save(item);
         }
         if (request.getQuantity() > item.getStock()) {
            return new MessageResponse("Out of stock",
                  HttpStatus.BAD_REQUEST.value(),
                  "ERROR");
         } else {
            item.setStock(item.getStock() - request.getQuantity());
            itemRepository.save(item);
         }


         Order order = Order.builder()
         .customer(customerRepository.findById(request.getCustomerId()).get())
               .item(itemRepository.findById(request.getItemId()).get()).quantity(request.getQuantity())
               .orderCode(generateOrderCode(request.getCustomerId(), request.getItemId()))
               .totalPrice(totalPrice)
               .orderDate(
                     new java.sql.Date(System.currentTimeMillis()))
         .build();
         orderRepository.save(order);

         
         Customer customer = customerRepository.findById(request.getCustomerId()).get();
         customer.setLastOrderDate(order.getOrderDate());
         customerRepository.save(customer);

         return new MessageResponse("Order has been successfuly added",
               HttpStatus.OK.value(),
               "OK");
      } catch (Exception e) {
         return new MessageResponse("Order Failed", HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR");
      }
   }

   public ResponseBodyDTO getOrderById(int orderId) {
      try {
         Optional<Order> orderOptional = orderRepository.findById(orderId);
         if (!orderOptional.isPresent()) {
            ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
            responseBodyDTO.setMessage("Order not found");
            responseBodyDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
            responseBodyDTO.setStatus("ERROR");
            return responseBodyDTO;
         } else {
            Order order = orderOptional.get();
            OrderListDto daftarOrderDTO = new OrderListDto();
            daftarOrderDTO.setOrderId(order.getOrderId());
            daftarOrderDTO.setOrderCode(order.getOrderCode());
            if (order.getOrderDate() != null) {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
               String formattedOrderDate = simpleDateFormat.format(order.getOrderDate());
               daftarOrderDTO.setOrderDate(formattedOrderDate);
            } else {
               daftarOrderDTO.setOrderDate(null);
            }
            CustomerListDto customerDTO = new CustomerListDto();
            Customer customer = order.getCustomer();
            customerDTO.setCustomerId(customer.getCustomerId());
            customerDTO.setCustomerName(customer.getCustomerName());
            customerDTO.setCustomerAddress(customer.getCustomerAddress());
            customerDTO.setCustomerCode(customer.getCustomerCode());
            customerDTO.setCustomerPhone(customer.getCustomerPhone());
            customerDTO.setIsActive(customer.getIsActive());
            if (customer.getLastOrderDate() != null) {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
               String formattedLastOrderDate = simpleDateFormat.format(customer.getLastOrderDate());
               customerDTO.setLastOrderDate(formattedLastOrderDate);
            } else {
               customerDTO.setLastOrderDate(null);
            }
            daftarOrderDTO.setCustomer(customerDTO);
            ItemListDto itemDTO = new ItemListDto();
            Item item = order.getItem();
            itemDTO.setItemId(item.getItemId());
            itemDTO.setItemName(item.getItemName());
            itemDTO.setItemCode(item.getItemsCode());
            itemDTO.setStock(item.getStock());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setIsAvailable(item.getIsAvailable());
            itemDTO.setLastReStock(item.getLastReStock());
            daftarOrderDTO.setItem(itemDTO);
            daftarOrderDTO.setQuantity(order.getQuantity());
            daftarOrderDTO.setTotalPrice(order.getTotalPrice());
            ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
            responseBodyDTO.setTotal(1);
            responseBodyDTO.setData(daftarOrderDTO);
            responseBodyDTO.setMessage("Order found");
            responseBodyDTO.setStatusCode(HttpStatus.OK.value());
            responseBodyDTO.setStatus("OK");
            return responseBodyDTO;
         }
      } catch (Exception e) {
         ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
         responseBodyDTO.setMessage("Order not found");
         responseBodyDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
         responseBodyDTO.setStatus("ERROR");

         return responseBodyDTO;
      }
   }

   @Transactional
   public MessageResponse updateOrder(UpdateOrderRequest request) {
      try {
         Optional<Order> optionalOrder = orderRepository.findById(request.getOrderId());
         Optional<Item> optionalItem = itemRepository.findById(request.getItemId());
         Optional<Customer> optionalCustomer = customerRepository.findById(request.getCustomerId());
         log.info("optionalOrder: " + optionalOrder);
         log.info("optionalItem: " + optionalItem);
         log.info("optionalCustomer: " + optionalCustomer);

         if (!optionalOrder.isPresent()) {
            return new MessageResponse("Order not found", HttpStatus.NOT_FOUND.value(), "ERROR");
         } else {
            Order order = optionalOrder.get();

            if (!optionalCustomer.isPresent()) {
               return new MessageResponse("Customer not found", HttpStatus.NOT_FOUND.value(), "ERROR");
            } else {
               Customer customer = optionalCustomer.get();
               customer.setLastOrderDate(order.getOrderDate());
               customerRepository.save(customer);
            }

            if (!optionalItem.isPresent()) {
               return new MessageResponse("Item not found", HttpStatus.NOT_FOUND.value(), "ERROR");
            } else {
               Item item = optionalItem.get();
               if (item.getStock() == 0) {
                  item.setIsAvailable(0);
                  itemRepository.save(item);
               }
               if (request.getQuantity() > item.getStock()) {
                  return new MessageResponse("Out of stock",
                        HttpStatus.BAD_REQUEST.value(),
                        "ERROR");
               } else {
                  int oldQuantity = order.getQuantity();
                  item.setStock(item.getStock() + oldQuantity);
                  item.setStock(item.getStock() - request.getQuantity());
                  itemRepository.save(item);
               }
               if (request.getQuantity() != 0) {
                  int totalPrice = request.getQuantity()
                        * itemRepository.findById(request.getItemId()).get().getPrice();
                  order.setTotalPrice(totalPrice);
                  order.setQuantity(request.getQuantity());
               }
            }

            order.setOrderCode(generateOrderCode(request.getCustomerId(),
                  request.getItemId()));

            orderRepository.save(order);
            return new MessageResponse("Update Successful", HttpStatus.OK.value(), "OK");

         }
      } catch (Exception e) {
         return new MessageResponse("Update Failed", HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR");
      }
   }

   @Transactional
   public MessageResponse deleteOrder(int orderId) {
      try {
         Optional<Order> optionalOrder = orderRepository.findById(orderId);
         log.info("optionalOrder: " + optionalOrder);

         if (!optionalOrder.isPresent()) {
            return new MessageResponse("Order not found", HttpStatus.NOT_FOUND.value(), "ERROR");
         } else {
            Order order = optionalOrder.get();
            orderRepository.delete(order);
            return new MessageResponse("Delete Successful", HttpStatus.OK.value(), "OK");
         }
      } catch (Exception e) {
         return new MessageResponse("Delete Failed", HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR");
      }
   }

   public ResponseEntity<Object> getAllOrder(Pageable pageable) {
      ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
      try {
         Page<Order> orderList = orderRepository.findAll(pageable);

         List<OrderListDto> response = orderList.stream().map(order -> {
            OrderListDto daftarOrderDTO = new OrderListDto();
            daftarOrderDTO.setOrderId(order.getOrderId());
            daftarOrderDTO.setOrderCode(order.getOrderCode());
            if (order.getOrderDate() != null) {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
               String formattedOrderDate = simpleDateFormat.format(order.getOrderDate());
               daftarOrderDTO.setOrderDate(formattedOrderDate);
            } else {
               daftarOrderDTO.setOrderDate(null);
            }
            CustomerListDto customerDTO = new CustomerListDto();
            Customer customer = order.getCustomer();
            customerDTO.setCustomerId(customer.getCustomerId());
            customerDTO.setCustomerName(customer.getCustomerName());
            customerDTO.setCustomerAddress(customer.getCustomerAddress());
            customerDTO.setCustomerCode(customer.getCustomerCode());
            customerDTO.setCustomerPhone(customer.getCustomerPhone());
            customerDTO.setIsActive(customer.getIsActive());
            if (customer.getLastOrderDate() != null) {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
               String formattedLastOrderDate = simpleDateFormat.format(customer.getLastOrderDate());
               customerDTO.setLastOrderDate(formattedLastOrderDate);
            } else {
               customerDTO.setLastOrderDate(null);
            }
            daftarOrderDTO.setCustomer(customerDTO);
            ItemListDto itemDTO = new ItemListDto();
            Item item = order.getItem();
            itemDTO.setItemId(item.getItemId());
            itemDTO.setItemName(item.getItemName());
            itemDTO.setItemCode(item.getItemsCode());
            itemDTO.setStock(item.getStock());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setIsAvailable(item.getIsAvailable());
            itemDTO.setLastReStock(item.getLastReStock());
            daftarOrderDTO.setItem(itemDTO);
            daftarOrderDTO.setQuantity(order.getQuantity());
            daftarOrderDTO.setTotalPrice(order.getTotalPrice());
            return daftarOrderDTO;
         }).collect(Collectors.toList());

         responseBodyDTO.setTotal(orderRepository.count());
         responseBodyDTO.setData(response);
         responseBodyDTO.setMessage("Order list found");
         responseBodyDTO.setStatusCode(HttpStatus.OK.value());
         responseBodyDTO.setStatus(HttpStatus.OK.name());

         return ResponseEntity.status(HttpStatus.OK).body(responseBodyDTO);

      } catch (Exception e) {
         responseBodyDTO.setMessage("Order list not found");
         responseBodyDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
         responseBodyDTO.setStatus("ERROR");
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBodyDTO);
      }
   }

   private String generateOrderCode(int customerId, int orderId) {
      int random = (int) (Math.random() * 9999);
      int calculate = customerId + orderId + random;
      return "ORD" + calculate;
   }
}
