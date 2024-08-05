package com.onlineshop.OnlineShop_BE.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.onlineshop.OnlineShop_BE.dto.request.CreateItemRequest;
import com.onlineshop.OnlineShop_BE.dto.request.UpdateItemRequest;
import com.onlineshop.OnlineShop_BE.dto.response.ItemListDto;
import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.dto.response.ResponseBodyDTO;
import com.onlineshop.OnlineShop_BE.model.Item;
import com.onlineshop.OnlineShop_BE.repository.ItemRepository;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private static final String ITEM_PREFIX = "ITM";
    @SuppressWarnings("unused")
   private static final String STATUS_OK = "OK";
    @SuppressWarnings("unused")
   private static final String STATUS_ERROR = "ERROR";
    private static final String MESSAGE_ITEM_NOT_FOUND = "Item not found";
    private static final String MESSAGE_ITEM_ADDED = "Item has been successfully added";
    private static final String MESSAGE_ITEM_ADD_FAILED = "Failed to add item";
    private static final String MESSAGE_ITEM_UPDATE_SUCCESS = "Update Successful";
    private static final String MESSAGE_ITEM_UPDATE_FAILED = "Update Failed";
    private static final String MESSAGE_ITEM_DELETE_SUCCESS = "Delete Success";
    private static final String MESSAGE_ITEM_DELETE_FAILED = "Delete Failed";
    private static final String MESSAGE_ITEM_LIST_FOUND = "Item List found";
    private static final String MESSAGE_ITEM_LIST_NOT_FOUND = "Item list not found";

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public MessageResponse createItem(CreateItemRequest request) {
        try {
            String itemCode = generateItemCode();
            Item item = buildItem(request, itemCode);
            itemRepository.save(item);
            return createMessageResponse(MESSAGE_ITEM_ADDED, HttpStatus.OK);
        } catch (Exception e) {
            return createMessageResponse(MESSAGE_ITEM_ADD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseBodyDTO getItemById(int itemId) {
        try {
            Optional<Item> itemOptional = itemRepository.findById(itemId);
            if (itemOptional.isPresent()) {
                Item item = itemOptional.get();
                ItemListDto itemDto = mapToItemDto(item);
                return createResponseBody(itemDto, 1, MESSAGE_ITEM_LIST_FOUND, HttpStatus.OK);
            } else {
                return createResponseBody(null, 0, MESSAGE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return createResponseBody(null, 0, MESSAGE_ITEM_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MessageResponse updateItem(UpdateItemRequest request) {
        try {
            Optional<Item> itemOptional = itemRepository.findById(request.getItemId());
            if (itemOptional.isPresent()) {
                Item item = updateItemDetails(itemOptional.get(), request);
                itemRepository.save(item);
                return createMessageResponse(MESSAGE_ITEM_UPDATE_SUCCESS, HttpStatus.OK);
            } else {
                return createMessageResponse(MESSAGE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return createMessageResponse(MESSAGE_ITEM_UPDATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MessageResponse deleteItem(int itemId) {
        try {
            Optional<Item> itemOptional = itemRepository.findById(itemId);
            if (itemOptional.isPresent()) {
                itemRepository.deleteById(itemId);
                return createMessageResponse(MESSAGE_ITEM_DELETE_SUCCESS, HttpStatus.OK);
            } else {
                return createMessageResponse(MESSAGE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return createMessageResponse(MESSAGE_ITEM_DELETE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getAllItem(Pageable pageable) {
        try {
            Page<Item> itemPage = itemRepository.findAll(pageable);
            List<ItemListDto> itemDtos = itemPage.stream()
                    .map(this::mapToItemDto)
                    .collect(Collectors.toList());

            ResponseBodyDTO responseBody = createResponseBody(itemDtos, (int) itemPage.getTotalElements(), MESSAGE_ITEM_LIST_FOUND, HttpStatus.OK);
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            ResponseBodyDTO responseBody = createResponseBody(null, 0, MESSAGE_ITEM_LIST_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    private String generateItemCode() {
        return ITEM_PREFIX + (int) (Math.random() * 9999);
    }

    private Item buildItem(CreateItemRequest request, String itemCode) {
        return Item.builder()
                .itemName(request.getItemName())
                .itemsCode(itemCode)
                .stock(request.getStock())
                .price(request.getPrice())
                .isAvailable(request.getIsAvailable())
                .lastReStock(request.getLastReStock())
                .build();
    }

    private Item updateItemDetails(Item item, UpdateItemRequest request) {
        if (isValid(request.getItemName())) {
            item.setItemName(request.getItemName());
        }
        if (isValid(request.getItemCode())) {
            item.setItemsCode(request.getItemCode());
        }
        if (request.getStock() != 0) {
            item.setStock(request.getStock());
        }
        if (request.getPrice() != 0) {
            item.setPrice(request.getPrice());
        }
        if (request.getIsAvailable() != 0) {
            item.setIsAvailable(request.getIsAvailable());
        }
        if (request.getLastReStock() != null) {
            item.setLastReStock(request.getLastReStock());
        }
        return item;
    }

    private boolean isValid(String value) {
        return value != null && !value.isEmpty();
    }

    private ItemListDto mapToItemDto(Item item) {
        return ItemListDto.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .itemCode(item.getItemsCode())
                .stock(item.getStock())
                .price(item.getPrice())
                .isAvailable(item.getIsAvailable())
                .lastReStock(item.getLastReStock())
                .build();
    }

    private MessageResponse createMessageResponse(String message, HttpStatus status) {
        return new MessageResponse(message, status.value(), status.getReasonPhrase());
    }

    private ResponseBodyDTO createResponseBody(Object data, int total, String message, HttpStatus status) {
        return ResponseBodyDTO.builder()
                .data(data)
                .total(total)
                .message(message)
                .statusCode(status.value())
                .status(status.getReasonPhrase())
                .build();
    }
}
