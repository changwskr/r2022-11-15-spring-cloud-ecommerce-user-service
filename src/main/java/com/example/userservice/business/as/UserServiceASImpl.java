package com.example.userservice.business.as;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.userservice.business.dc.UserServiceDC;
import com.example.userservice.web.transfer.vo.ResponseUserFormVO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.userservice.business.client.OrderServiceClient;
//import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.transfer.dto.UserDto;
import com.example.userservice.business.dc.dao.model.UserEntity;
import com.example.userservice.business.dc.dao.IUserRepositoryDAO;
import com.example.userservice.web.transfer.vo.ResponseOrderFormVO;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceASImpl implements UserServiceAS {
    IUserRepositoryDAO IUserRepositoryDAO;
    BCryptPasswordEncoder passwordEncoder;
    UserServiceDC userServiceDC;

    Environment env;
    RestTemplate restTemplate;
    
    OrderServiceClient orderServiceClient;

    CircuitBreakerFactory circuitBreakerFactory;


    public UserServiceASImpl(IUserRepositoryDAO IUserRepositoryDAO,
                             BCryptPasswordEncoder passwordEncoder, // passwordEncoder은 아직 빈으로 등록된 적이 없다. 기동 클래스에서 빈드로 말들자
                             UserServiceDC userServiceDC,
                             Environment env,
                             RestTemplate restTemplate,
                             OrderServiceClient orderServiceClient,
                             CircuitBreakerFactory circuitBreakerFactory) {
        this.IUserRepositoryDAO = IUserRepositoryDAO;
        this.passwordEncoder = passwordEncoder;
        this.userServiceDC = userServiceDC;
        this.env = env;
        this.restTemplate = restTemplate;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        UserDto returnUserDto = userServiceDC.insertUser(userDto);

        return returnUserDto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
    	
    	log.debug("----------------------->getUserByUserId start");
        UserDto userDto = userServiceDC.findByUserId(userId);

        {
        	String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders";
        }

        /* ---------------------------------------------------------------------------------------- *
         * Order-Service를 호출하는 방법은
         * 1) Rest Template
         * 2) Feign Client
         * 3) Circuit Braker         
         * --------------------------------------------------------------------------------------- */
        
        boolean foo_t1 = false;
        boolean foo_t2_1 = false; // feign 예외직접 처리
        boolean foo_t2_2 = false; // feign 예외자체 처리 - FeignErrorDecoder
        boolean foo_t3 = false; // circuit 적용
        
        List<ResponseOrderFormVO> ordersList = null;
        
        if( foo_t1 ) {
        	
	        /* 1) Using as rest template */
	        // exchange(url,GET,requestEntity,받아오고자하는 데이타타입)
	        // order_service.url은 user-service.yml에 선언한다.
	        // url: http://127.0.0.1:8000/order-service/%s/orders
	        List<ResponseOrderFormVO> orders = new ArrayList<>();
	        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
	        log.debug("--->orderUrl>"+orderUrl);
	        
	        ResponseEntity<List<ResponseOrderFormVO>> orderListResponse =
	                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
	                                            new ParameterizedTypeReference<List<ResponseOrderFormVO>>() {
	                });
	        
	        ordersList = orderListResponse.getBody();
	        log.debug("--->ordersList>"+ordersList);
        }
        else if(foo_t2_1) {
	        /* 2) Using a feign client */
	        /* Feign exception handling */
	        /* ErrDecoder에서 다음의 예외처리를 해주므로 다음의 해결책으로 정리한다. */        	
	        try {
		        log.debug("--->orderServiceClient>"+orderServiceClient);

	            ordersList = orderServiceClient.getOrders(userId);
	        } catch (FeignException ex) {
	        	log.error("--Feign client 호출에러");
	            log.error(ex.getMessage());
	        }
        }
        else if(foo_t2_2) {
	        /* ErrorDecoder */
        	// 에러디코더를 사용하면 자체내에 예외처리 가이드가 기술되어 있다.
	        log.info("Before call orders microservice");
        	ordersList = orderServiceClient.getOrders(userId);
        }
        else if(foo_t3) {
	        /*
	         * 3) circuit 적용
	         */
	        log.info("★★★ Before call orders microservice");
			CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");			
			ordersList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
					throwable -> new ArrayList<>()); // 만약 order서비스가 비정상적이라면, new ArrayList<>()이라는 아무것도 없는 값을 리턴한다. 
			log.info("★★★ After called orders microservice");
        }
        else{
            ordersList = new ArrayList();
        }

        userDto.setOrders(ordersList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userServiceDC.findAll();
    }

    @Override
    public Iterable<UserDto> getUserByAll2() {

        Iterable<UserEntity> userEntityList = userServiceDC.findAll();

        log.info("★★★ userEntityList>" + userEntityList);

        List<UserDto> result = new ArrayList<>();
        userEntityList.forEach(v -> {
            result.add(new ModelMapper().map(v, UserDto.class));
        });

        return result;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = IUserRepositoryDAO.findByEmail(email);
        if (userEntity == null)
            // return null;
            throw new UsernameNotFoundException(email);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(userEntity, UserDto.class);
        return userDto;
    }

    // username으로 여기서는 userid로 사용자를 한명 찾아오는 역할이 필요하다.
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = IUserRepositoryDAO.findByEmail(username);

        log.info("### 로그인 요청 -WebSecurity.loadUserByUsername()## - UserServiceImpl.loadUserByUsername()--start-" + username);

        if (userEntity == null) {
            log.error("### userEntity is null###################### ");
            throw new UsernameNotFoundException(username + ": not found");
        }

        User user = new User(   userEntity.getEmail(),
                                userEntity.getEncryptedPwd(),
                                true,
                                true,
                                true,
                                true,
                                new ArrayList<>()   );
        
        log.info("### 로그인 요청 -WebSecurity.configure()## - UserServiceImpl.loadUserByUsername()--end-" + user);
        
        return user;
	}

	public void test() {
		
		//http://127.0.0.1:8000/order-service/58637473-1c45-463e-8378-f9eb09cb279c/orders
			
        URI uri = UriComponentsBuilder
                .fromUriString("http://127.0.0.1:8000")
                .path("/order-service/58637473-1c45-463e-8378-f9eb09cb279c/orders")
                .build()
                .toUri();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

            log.debug("status code : {}", responseEntity.getStatusCode());
            log.info("body : {}", responseEntity.getBody());

            responseEntity.getBody();

	}


}
