package com.example.userservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.userservice.business.as.UserServiceAS;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private UserServiceAS userServiceAS;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Environment env;

    private static final String[] PERMIT_URL_ARRAY = {
            /* swagger v2 */
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            /* swagger v3 */
            "/v3/api-docs/**",
            "/h2-console/**",
            "/swagger-ui/**",
            "/**/**",  //현재 모든 페이지에 대해서 오픈 향후 변경 필요
    };
    
    
    public WebSecurity(Environment env, UserServiceAS userServiceAS, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.env = env;
        this.userServiceAS = userServiceAS;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        log.info("■■■ WebSecurity.configure start ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");

        // 기본 통과
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().antMatchers("/health_check/**").permitAll();
        http.authorizeRequests().antMatchers("/swagger-resources/**").permitAll();
        http.authorizeRequests().antMatchers("/v2/api-docs").permitAll();
        http.authorizeRequests().antMatchers("/swagger-resources").permitAll();
        http.authorizeRequests().antMatchers("/swagger-resources/**").permitAll();
        http.authorizeRequests().antMatchers("/configuration/ui").permitAll();
        http.authorizeRequests().antMatchers("/configuration/security").permitAll();
        http.authorizeRequests().antMatchers("/webjars/**").permitAll();
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll();
        http.authorizeRequests().antMatchers("/swagger-ui/**").permitAll();



        Boolean AUTH_TYPE_01 = Boolean.TRUE; // users 서비스 허용
        Boolean AUTH_TYPE_02 = Boolean.FALSE; // 특정 IP 만 접근 허용
        Boolean AUTH_TYPE_03 = Boolean.FALSE;
        Boolean AUTH_TYPE_04 = Boolean.FALSE;

        if(AUTH_TYPE_01){
            log.info("■■■ AUTH_TYPE_01 - TRUE ");
            http.authorizeRequests() // 보호된 자원에 대한 접근 권한을 설정한다.
                    .antMatchers("/users/**") // 이 패턴을 모든 권한을 준다.
                    .permitAll();
        }


        if(AUTH_TYPE_02){
            log.info("■■■ AUTH_TYPE_02 - TRUE ");
            // 결론 127.0.0.1 인 IP만을 인증할것이고. 추가로 getAuthenticationFilter()에 결려있는 필터를 적용해 주세요
            // 192.168.0.8로 하니깐 에러가 발생하는데 127로 하니 에러는 발생하지 않네

            http.authorizeRequests() // 보호된 리소스에 대한 접근 권한을 설정한다
                    .antMatchers("/**")
                    .hasIpAddress("127.0.0.1") // 이 IP만 허용하고 다른 IP는 제약을 걸어보자
                    .and()
                    .addFilter(getAuthenticationFilter()); //WebSecurity.getAuthenticationFilter() 함수를 만들어 필터작용을 한다.
        }

        if(AUTH_TYPE_04){
            log.info("■■■ AUTH_TYPE_02 - TRUE ");
            // getAuthenticationFilter()에 결려있는 필터를 적용해 주세요
            // 192.168.0.8로 하니깐 에러가 발생하는데 127로 하니 에러는 발생하지 않네

            http.authorizeRequests() // 보호된 리소스에 대한 접근 권한을 설정한다
                    .antMatchers("/**")
                    .hasIpAddress("172.*.*.*") // 이 IP만 허용하고 다른 IP는 제약을 걸어보자
                    .and()
                    .addFilter(getAuthenticationFilter()); //WebSecurity.getAuthenticationFilter() 함수를 만들어 필터작용을 한다.
        }

        if(AUTH_TYPE_03){
            log.info("■■■ AUTH_TYPE_03 - TRUE ");
            http.authorizeRequests() // 보호된 자원에 대한 접근 권한을 설정한다.
                    .antMatchers("/**") // 이 패턴을 모든 권한을 준다.
                    .permitAll();
        }

        http.headers().frameOptions().disable();

        log.info("■■■ WebSecurity.configure end ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");
    }

    protected void configure_T01(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/users/**").permitAll();
        http.headers().frameOptions().disable();
    }


//    @Override
    protected void configure_최종(HttpSecurity http) throws Exception {

        System.out.println("##권한을 넣는다.####################### - WebSecurity.configure()--start");
        http.csrf().disable();

        // 기본 통과
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().antMatchers("/health_check/**").permitAll();
        http.authorizeRequests().antMatchers(PERMIT_URL_ARRAY).permitAll();
        http.authorizeRequests().antMatchers("/swagger-resources/**").permitAll();

        boolean 권한_전체_허용 = false;
        boolean 권한_ip_filter_허용=true;

        if(권한_전체_허용) {
            // 권한 users로 요청하는 전체 통과 적용
            http.authorizeRequests().antMatchers("/users/**").permitAll();
        }

        if(권한_ip_filter_허용) {
            // 결론 127.0.0.1 인 IP만을 인증할것이고. 추가로 getAuthenticationFilter()에 결려있는 필터를 적용해 주세요
            // 192.168.0.8로 하니깐 에러가 발생하는데 127로 하니 에러는 발생하지 않네

            http.authorizeRequests().antMatchers("/**")
                    .hasIpAddress("127.0.0.1") // IP에 대한 제약을 걸어보자
                    .and()
                    .addFilter(getAuthenticationFilter()); //WebSecurity.getAuthenticationFilter() 함수를 만들어 필터작용을 한다.
        }


        http.headers().frameOptions().disable();

        System.out.println("########################## - WebSecurity.configure()--end");
    }

    //@Override
	protected void configure_blog(HttpSecurity http) throws Exception {
		http
			.csrf().disable()  // csrf 토큰 비활성화 (테스트시 걸어두는 게 좋음)
			.authorizeRequests()
				.antMatchers("/", "/auth/**", "/js/**", "/css/**", "/image/**", "/dummy/**") 
				.permitAll()
				.anyRequest()
				.authenticated()
			.and()
				.formLogin()
				.loginPage("/auth/loginForm")
				.loginProcessingUrl("/auth/loginProc")
				.defaultSuccessUrl("/"); // 스프링 시큐리티가 해당 주소로 요청오는 로그인을 가로채서 대신 로그인 해준다.
	}

    //@Override
    protected void configure_tst(HttpSecurity http) throws Exception {
        http.csrf().disable();
//        http.authorizeRequests().antMatchers("/users/**").permitAll();
        System.out.println("--->gateway.ip>");

        System.out.println("--->gateway.ip>" + env.getProperty("gateway.ip"));
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().antMatchers("/health_check/**").permitAll();
        http.authorizeRequests().antMatchers("/**")
                .hasIpAddress("127.0.0.1") // <- IP 변경
                .and()
                .addFilter(getAuthenticationFilter());

//        http.authorizeRequests().antMatchers("/users")
//                .hasIpAddress(env.getProperty("gateway.ip")) // <- IP 변경
//                .and()
//                .addFilter(getAuthenticationFilter());
//
//        http.authorizeRequests().anyRequest().denyAll();

        http.headers().frameOptions().disable();
    }

    // 인증처리를 한다.
    // 실제 우리는 spring security의 login기능을 활용해서 처리를 한다.
    private AuthenticationFilter getAuthenticationFilter() throws Exception {
    	/*
    	 * 여기서 권한을 위한 필터 작업을 한다. 제약을 거는 작업을 한다는 의미이다.
    	 */

    	// 인증처리를 한다.
    	log.info("### 권한필터 적용## - WebSecurity.getAuthenticationFilter()--start");
    	
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(authenticationManager(), userServiceAS, env);

//        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
//        authenticationFilter.setAuthenticationManager(authenticationManager());

        log.info("### 권한필터 적용##ebSecurity.getAuthenticationFilter()--end");
        
        return authenticationFilter;
    }

    // select pwd from users where email=?
    // db_pwd(encrypted) == input_pwd(encrypted)
    /*
     * 여기서 인증에 관련된 작업을 한다.
     */
//    WebSecurity.java의 configure에서 인증처리를 한다. 여기서 중요한 점은
//    현재 user-service에 저장된 패스워드는 암복호화 된 정보이다. 그런데 현재 configure 요청 시점의 패워드는 정보는 클라이언트에서 요청한
//    플레인텍스트여서 이것을 암호화로 바꾸어서 상호 같은지 비교하는 기능이 필요하다. 

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	
    	System.out.println("로그인 인증 처리 # - WebSecurity.configure()--start--AuthenticationManagerBuilder");
    	
    	// 여기서 패스워드가 같은지 비교한다.
    	// 여기서 인증객체를 만들기 위해서는 UserDetailsService를 상속해서 구현해야 된다는 점이다.
    	// auth.userDetailsService(userService) 여기서 인증처리를 한다.
    	// passwordEncoder(bCryptPasswordEncoder) 을 통해서 패스워드 같은지 비교한다.
        auth.userDetailsService(userServiceAS).passwordEncoder(bCryptPasswordEncoder);
        
        System.out.println("로그인 인증 처리 # - WebSecurity.configure()--end--AuthenticationManagerBuilder");
        
    }


}
