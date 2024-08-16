package com.example.userservice.transfer.vo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class GreetingVO {
    @Value("${greeting.message}")
    private String message;
}
