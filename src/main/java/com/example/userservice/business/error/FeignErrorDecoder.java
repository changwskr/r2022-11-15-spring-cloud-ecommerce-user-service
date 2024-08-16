package com.example.userservice.business.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    Environment env;

    @Autowired
    public FeignErrorDecoder(Environment env) {
        this.env = env;
    }


	public FeignErrorDecoder() {
		// TODO Auto-generated constructor stub
	}

	@Override
    public Exception decode(String methodKey, Response response) {
        switch(response.status()) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                    		// 2022-11-29-1 user-service.yml에 order.exception 관련내용을 명시해서 사용하자
                            env.getProperty("order_service.exception.orders_is_empty")); // user-service.yml
                }
                break;
            default:
                return new Exception(response.reason());
        }

        return null;
    }
}
