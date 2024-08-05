package com.onlineshop.OnlineShop_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBodyDTO {
	private long total;
	private Object data;
	private String message;
	private int statusCode;
	private String status;
}