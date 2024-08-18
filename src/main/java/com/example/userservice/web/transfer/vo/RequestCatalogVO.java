package com.example.userservice.web.transfer.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestCatalogVO {
	
    @NotNull(message = "productId cannot be null")
    @Size(min = 2, message = "productId not be less than two characters")
    private String productId;
    private Integer qty;
    private String productName;
    private Integer unitPrice;
    private Integer stock;
  
}
