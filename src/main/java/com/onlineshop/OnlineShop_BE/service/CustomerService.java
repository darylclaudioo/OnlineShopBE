package com.onlineshop.OnlineShop_BE.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import lib.minio.MinioSrvc;
import lib.minio.exception.MinioServiceException;

import org.springframework.web.multipart.MultipartFile;

import com.onlineshop.OnlineShop_BE.dto.request.CreateCustomerRequest;
import com.onlineshop.OnlineShop_BE.dto.request.UpdateCustomerRequest;
import com.onlineshop.OnlineShop_BE.dto.response.CustomerListDto;
import com.onlineshop.OnlineShop_BE.dto.response.MessageResponse;
import com.onlineshop.OnlineShop_BE.dto.response.ResponseBodyDTO;
import com.onlineshop.OnlineShop_BE.model.Customer;
import com.onlineshop.OnlineShop_BE.repository.CustomerRepository;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final MinioSrvc minioSrvc;
    private static final String CUSTOMER_PREFIX = "CUST";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    public CustomerService(CustomerRepository customerRepository, MinioSrvc minioSrvc) {
        this.customerRepository = customerRepository;
        this.minioSrvc = minioSrvc;
    }

    @Transactional
    public MessageResponse createCustomer(CreateCustomerRequest request, MultipartFile file) {
        try {
            String customerCode = generateCustomerCode();
            String imageFileName = uploadCustomerImage(request, file);

            Customer customer = buildCustomer(request, customerCode, imageFileName);
            customerRepository.save(customer);

            return createMessageResponse("Customer has been successfully added", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to add customer", e);
            return createMessageResponse("Failed to add customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseBodyDTO getCustomerById(int customerId) {
        try {
            Optional<Customer> customerOptional = customerRepository.findById(customerId);
            if (customerOptional.isPresent()) {
                Customer customer = customerOptional.get();
                CustomerListDto customerDto = mapToCustomerDto(customer);

                return createResponseBody(customerDto, 1, "Customer found", HttpStatus.OK);
            } else {
                return createResponseBody(null, 0, "Customer not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching customer by ID", e);
            return createResponseBody(null, 0, "An error occurred while retrieving customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MessageResponse updateCustomer(UpdateCustomerRequest request, MultipartFile file) {
        try {
            Optional<Customer> customerOptional = customerRepository.findById(request.getCustomerId());
            if (customerOptional.isPresent()) {
                Customer customer = updateCustomerDetails(customerOptional.get(), request, file);
                customerRepository.save(customer);

                return createMessageResponse("Update Successful", HttpStatus.OK);
            } else {
                return createMessageResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Update failed", e);
            return createMessageResponse("Update Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public MessageResponse deleteCustomer(int customerId) {
        try {
            Optional<Customer> customerOptional = customerRepository.findById(customerId);
            if (customerOptional.isPresent()) {
                customerRepository.delete(customerOptional.get());
                return createMessageResponse("Delete Successful", HttpStatus.OK);
            } else {
                return createMessageResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Delete failed", e);
            return createMessageResponse("Delete Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getAllCustomer(Pageable pageable) {
        ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
        try {
            Page<Customer> customerPage = customerRepository.findAll(pageable);

            List<CustomerListDto> customerDtos = customerPage.stream()
                    .map(this::mapToCustomerDto)
                    .collect(Collectors.toList());

            responseBodyDTO.setTotal((int) customerPage.getTotalElements());
            responseBodyDTO.setData(customerDtos);
            responseBodyDTO.setMessage("Customer list found");
            responseBodyDTO.setStatusCode(HttpStatus.OK.value());
            responseBodyDTO.setStatus(HttpStatus.OK.name());

            return ResponseEntity.ok(responseBodyDTO);

        } catch (Exception e) {
            log.error("Failed to retrieve customer list", e);
            responseBodyDTO.setMessage("Customer list not found");
            responseBodyDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseBodyDTO.setStatus("ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBodyDTO);
        }
    }

    private String generateCustomerCode() {
        return CUSTOMER_PREFIX + new Random().nextInt(9999);
    }

    private String uploadCustomerImage(CreateCustomerRequest request, MultipartFile file) throws MinioServiceException {
        try {
            return minioSrvc.uploadImageToMinio(request, file);
        } catch (Exception e) {
            String errorMessage = "Failed to upload file";
            log.error(errorMessage, e);
            throw new MinioServiceException(errorMessage, e);
        }
    }

    private Customer buildCustomer(CreateCustomerRequest request, String customerCode, String imageFileName) {
        return Customer.builder()
                .customerName(request.getCustomerName())
                .customerAddress(request.getCustomerAddress())
                .customerCode(customerCode)
                .customerPhone(request.getCustomerPhone())
                .isActive(request.getIsActive())
                .pic(imageFileName)
                .build();
    }

    private Customer updateCustomerDetails(Customer customer, UpdateCustomerRequest request, MultipartFile file) throws MinioServiceException, IOException {
        if (isValid(request.getCustomerName())) {
            customer.setCustomerName(request.getCustomerName());
        }
        if (isValid(request.getCustomerAddress())) {
            customer.setCustomerAddress(request.getCustomerAddress());
        }
        if (isValid(request.getCustomerCode())) {
            customer.setCustomerCode(request.getCustomerCode());
        }
        if (isValid(request.getCustomerPhone())) {
            customer.setCustomerPhone(request.getCustomerPhone());
        }
        customer.setIsActive(request.getIsActive());

        if (file != null && !file.isEmpty()) {
            String newImageFileName = minioSrvc.updateImageToMinio(request, file);
            customer.setPic(newImageFileName);
        }
        return customer;
    }

    private boolean isValid(String value) {
        return value != null && !value.isEmpty();
    }

    private CustomerListDto mapToCustomerDto(Customer customer) {
        CustomerListDto customerDto = new CustomerListDto();
        customerDto.setCustomerId(customer.getCustomerId());
        customerDto.setCustomerName(customer.getCustomerName());
        customerDto.setCustomerAddress(customer.getCustomerAddress());
        customerDto.setCustomerCode(customer.getCustomerCode());
        customerDto.setCustomerPhone(customer.getCustomerPhone());
        customerDto.setIsActive(customer.getIsActive());
        customerDto.setPic(getImageUrl(customer.getPic()));

        if (customer.getLastOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            customerDto.setLastOrderDate(sdf.format(customer.getLastOrderDate()));
        }
        return customerDto;
    }

    private MessageResponse createMessageResponse(String message, HttpStatus status) {
        return new MessageResponse(message, status.value(), status.getReasonPhrase());
    }

    private ResponseBodyDTO createResponseBody(Object data, int total, String message, HttpStatus status) {
        ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
        responseBodyDTO.setData(data);
        responseBodyDTO.setTotal(total);
        responseBodyDTO.setMessage(message);
        responseBodyDTO.setStatusCode(status.value());
        responseBodyDTO.setStatus(status.getReasonPhrase());
        return responseBodyDTO;
    }

    private String getImageUrl(String filename) {
        return filename != null ? minioSrvc.getPublicLink(filename) : "";
    }
}
