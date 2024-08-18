package com.example.userservice.tpm;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.userservice.web.transfer.vo.ResponseOrderFormVO;

// 요청하고자 하는 서비스 명시
@FeignClient(name="order-service")
public interface TPSsendrecvOrderService {

	// 요청하고자 하는 서비스 형식 명시
    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrderFormVO> getOrders(@PathVariable String userId);



}
