package com.example.userservice.web.transfer.vo;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/*
 * 클라이언트에서 서버로 전송되는 데이타 저장하는 클래스
 */
@Data
public class RequestLoginFormVO {
    @NotNull(message = "Email cannot be null")
    @Size(min = 2, message = "Email not be less than two characters")
    @Email
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be equals or greater than 8 characters")
    private String password;

}
