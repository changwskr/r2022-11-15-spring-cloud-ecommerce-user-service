package com.example.userservice.web.transfer.vo;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class RequestUserFormVO {

    /**
     * 이 정보 RequestBody 정보에 대한 매핑 정보를 관리하는 것
     * 어노테이션을 사용해서 데이타에 대한 무결성을 확인할 수 도 있다.
     */
    @NotNull(message = "Email cannot be null")
    @Size(min = 2, message = "Email not be less than two characters")
    @Email
    private String email;

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, message = "Name not be less than two characters")
    private String name;

    @NotNull(message = "Password cannot be null")
    @Size(min = 4, message = "Password must be equal or grater than 8 characters")
    private String pwd;
}
