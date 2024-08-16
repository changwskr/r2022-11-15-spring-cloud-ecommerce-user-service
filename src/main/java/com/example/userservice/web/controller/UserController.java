package com.example.userservice.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.userservice.business.as.UserServiceAS;
import com.example.userservice.transfer.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.business.dc.dao.model.UserEntity;
import com.example.userservice.transfer.vo.GreetingVO;
import com.example.userservice.web.transfer.vo.RequestUserFormVO;
import com.example.userservice.web.transfer.vo.ResponseUserFormVO;

import io.micrometer.core.annotation.Timed;



// 2022-11-27-9 게이트웨이에 필터를 적용해서 /user-service 방식 //으로 변경한다.
// @RequestMapping("/user-service")을 @RequestMapping("/")

@RestController
@RequestMapping("/")
public class UserController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Environment env;
    private UserServiceAS userServiceAS;

    @Autowired
    private GreetingVO greetingVO;

    @Autowired // 빈주입
    public UserController(Environment env, UserServiceAS userServiceAS) {
        this.env = env;
        this.userServiceAS = userServiceAS;
    }

    /*
     * 2022-11-26-8-1
     * http://localhost:xxxx/heal_check
     */
    @GetMapping("/health_check")
    @Timed(value="users.status", longTask = true)
    public String status() {
        return String.format("It's Working in User Service"
                + ", port(local.server.port)=" + env.getProperty("local.server.port")
                + ", port(server.port)=" + env.getProperty("server.port")
                + ", gateway ip=" + env.getProperty("gateway.ip")
                + ", message=" + env.getProperty("greeting.message")
                + ", token secret=" + env.getProperty("token.secret")
                + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

	/*
	 * 2022-11-26-8-2
	 * 그리팅 메시지를 출력해주세요
	 * http://localhost:xxxx/welcome
	 */
    @GetMapping("/welcome")
    @Timed(value="users.welcome", longTask = true)
    public String welcome(HttpServletRequest request, HttpServletResponse response) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            Arrays.stream(cookies).forEach(cookie -> {
//                System.out.print(cookie.getName() + "=" + cookie.getValue());
//            });
//        }
//        Cookie c1 = new Cookie("myuser_token", "abcd1234");
//        response.addCookie(c1);
//        return env.getProperty("greeting.message");
    	
    	// 2022-11-26-8-2 실제 env를 통해서 접근
//    	return env.getProperty("greeting.message");
    	
    	// 2022-11-26-8-3 com.example.userservice.vo.Greeting을 이용하여 환경파일의 정보 출력
    	 return greetingVO.getMessage();
    }

    // 사용자 정보 등록
    @PostMapping("/users")
    public ResponseEntity<ResponseUserFormVO> createUser(@RequestBody RequestUserFormVO user) {
    	
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // mapper를 통해서 매핑
        UserDto userDto = mapper.map(user, UserDto.class);
        // 서비스 요청
        userServiceAS.createUser(userDto);
        // mapper를 통해서 매핑
        ResponseUserFormVO responseUserFormVO = mapper.map(userDto, ResponseUserFormVO.class);
        // 클라이언트로 리턴 201
        // body에 ResponseUser를 셋팅한다.
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUserFormVO);
    }

    // 전체 사용자 조회
    @GetMapping("/users")
    public ResponseEntity<List<ResponseUserFormVO>> getUsers() {
        Iterable<UserEntity> userList = userServiceAS.getUserByAll();
        
        log.info("★★★ userList>" + userList);

        List<ResponseUserFormVO> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUserFormVO.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users2")
    public ResponseEntity<List<ResponseUserFormVO>> getUsers2() {
        Iterable<UserDto> userList = userServiceAS.getUserByAll2();

        log.info("★★★ user2-userList>" + userList);

        List<ResponseUserFormVO> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUserFormVO.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    // 사용자 정보, 주문 내역조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUserFormVO> getUser(@PathVariable("userId") String userId) {
    	log.info("★★★ userId>" + userId);

    	UserDto userDto = userServiceAS.getUserByUserId(userId);

    	log.info("★★★ userDto>" + userDto);

        ResponseUserFormVO returnValue = new ModelMapper().map(userDto, ResponseUserFormVO.class);

        // 정상적인 것은 200이나 보다 정확도를 높이려면 201를 넣는 것이 맞다.
        // body에 ResponseUser를 셋팅한다.
        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }
}
