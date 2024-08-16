package com.example.userservice.web.transfer.vo;

import java.util.Date;

import lombok.Data;

@Data
public class ResponseOrderFormVO {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private Date createdAt;

    private String orderId;
}
