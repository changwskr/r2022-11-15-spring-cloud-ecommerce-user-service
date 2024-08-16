package com.example.userservice.business.dc;


import com.example.userservice.business.dc.dao.model.UserEntity;
import com.example.userservice.transfer.dto.UserDto;
import com.example.userservice.web.transfer.vo.ResponseOrderFormVO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.userservice.business.dc.dao.IUserRepositoryDAO;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserServiceDC {
    private IUserRepositoryDAO IUserRepositoryDAO;
    BCryptPasswordEncoder passwordEncoder;

    public UserServiceDC(IUserRepositoryDAO IUserRepositoryDAO, BCryptPasswordEncoder passwordEncoder) {
        this.IUserRepositoryDAO = IUserRepositoryDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto insertUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        log.info("★★★" + userDto);

        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
//      userEntity.setEncryptedPwd(userDto.getPwd());

        IUserRepositoryDAO.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);

        return returnUserDto;
    }

    public UserDto findByUserId(String userId) {

        log.debug("----------------------->getUserByUserId start");
        UserEntity userEntity = IUserRepositoryDAO.findByUserId(userId);

        if (userEntity == null)
            throw new UsernameNotFoundException("User not found");

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        return userDto;
    }

    public Iterable<UserEntity> findAll() {
        return IUserRepositoryDAO.findAll();
    }

    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = IUserRepositoryDAO.findByEmail(email);
        if (userEntity == null)
            return null;
        //throw new UsernameNotFoundException(email);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(userEntity, UserDto.class);
        return userDto;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = IUserRepositoryDAO.findByEmail(username);

        System.out.println("로그인 요청 -WebSecurity.configure()## - UserServiceImpl.loadUserByUsername()--start-" + username);

        if (userEntity == null)
            throw new UsernameNotFoundException(username + ": not found");


        User user = new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());

        System.out.println("로그인 요청 -WebSecurity.configure()## - UserServiceImpl.loadUserByUsername()--end-" + user);

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
