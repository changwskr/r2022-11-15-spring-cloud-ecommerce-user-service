package com.example.userservice.transfer.dto;

import java.util.Date;
import java.util.List;

import com.example.userservice.web.transfer.vo.ResponseOrderFormVO;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;

    private String decryptedPwd;

    private String encryptedPwd;

    private List<ResponseOrderFormVO> orders;
}
