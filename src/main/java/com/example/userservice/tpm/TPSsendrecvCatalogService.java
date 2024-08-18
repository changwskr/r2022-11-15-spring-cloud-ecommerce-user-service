package com.example.userservice.tpm;

import com.example.userservice.web.transfer.vo.ResponseCatalogVO;
import com.example.userservice.web.transfer.vo.ResponseOrderFormVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

// 요청하고자 하는 서비스 명시
@FeignClient(name="catalog-service")
public interface TPSsendrecvCatalogService {
    @GetMapping("/catalog-service/catalogs")
    List<ResponseCatalogVO> getCatalogs();
}

