package com.example.userservice.business.dc.dao;

import com.example.userservice.business.dc.dao.model.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface IUserRepositoryDAO extends CrudRepository<UserEntity, Long> {
    UserEntity findByUserId(String userId);
    UserEntity findByEmail(String username);
}
