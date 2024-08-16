package com.example.userservice.security;

import com.example.userservice.transfer.dto.UserDto;
import com.example.userservice.business.as.UserServiceAS;
import com.example.userservice.web.transfer.vo.RequestLoginFormVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private UserServiceAS userServiceAS;
    private Environment env;


    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserServiceAS userService,
                                Environment env) {
        super.setAuthenticationManager(authenticationManager);
        this.userServiceAS = userService;
        this.env = env;
    }

    /*
     * 1. 사용자의 요청정보는 RequestLogin 객체로 온다.
     * 2. 서비스 시작전 적용된다.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
        	log.info("### - AuthenticationFilter.attemptAuthentication()--start");
        	
        	// 전달되는 스트림을 찾아서 RequestLogin 객체로 전환시킨다.
            // RequestLoginFormVO 객체내에서 id/pw에 해당되는 것을 찾는다.
            RequestLoginFormVO creds = new ObjectMapper().readValue(request.getInputStream(), RequestLoginFormVO.class);
            

            // 토큰값으로 변경해서
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(),   new ArrayList<>());
            // 인증작업을 요청한다.

        	log.info("### - AuthenticationFilter.attemptAuthentication()--end--" + token.toString() );

            return getAuthenticationManager().authenticate(token);
            
        } catch(IOException e) {
        	log.error("### - AuthenticationFilter.attemptAuthentication()--exception");
            throw new RuntimeException(e);
        }
    }

    /*
     * 실제 성공을 했을때 어떻게 할 것인가를 정의한다.
     * 여기서 토큰을 발행한다.
     * 그리고 토큰을 클라이언트로 보낸다. 토큰값은 헤더에 저장되어진다.
     * postman으로 확인하세요 header - token
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
    	
    	log.info("### - 인증이후 토큰을 생성해서 헤더에 저장한다. AuthenticationFilter.successfulAuthentication()--start");
        String userName = ((User)authResult.getPrincipal()).getUsername();
        log.info("###"+ userName + "]");
        
        log.info("### - AuthenticationFilter.successfulAuthentication()--userService.getUserDetailsByName() 호출");
        UserDto userDetails = userServiceAS.getUserDetailsByEmail(userName);

        // 토큰을 생성한다. - userName으로 userId를 찾아서 userid로 토큰을 생성한다.
        log.info("### - AuthenticationFilter.successfulAuthentication()--토큰 생성 요청");
        String token = Jwts.builder()
                .setSubject(userDetails.getUserId())
                .setExpiration(new Date(System.currentTimeMillis() +
                        Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();

        // 토큰을 헤더에 저장한다.
        log.info("### 생성된 토큰을 헤더에 저장한다. token, userid");
        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());

        log.info("### - AuthenticationFilter.successfulAuthentication()--end");
    }
}
