package com.example.userservice.business.as;

import org.springframework.security.core.userdetails.UserDetailsService;

//import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.userservice.transfer.dto.UserDto;
import com.example.userservice.business.dc.dao.model.UserEntity;

public interface UserServiceAS extends UserDetailsService {
    UserDto createUser(UserDto userDto);

    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();
    Iterable<UserDto> getUserByAll2();

    UserDto getUserDetailsByEmail(String userName);
	
}
