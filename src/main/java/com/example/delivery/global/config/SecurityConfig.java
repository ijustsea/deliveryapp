package com.example.delivery.global.config;

import com.example.delivery.global.security.filter.JwtAuthenticationFilter;
import com.example.delivery.global.security.filter.LoginAuthenticationFilter;
import com.example.delivery.global.security.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {

    // PasswordEncoder 타입의 BCryptPasswordEncoder를 bean 등록
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    // Spring Security 인증 총괄용 AuthenticationManager를 Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
    // SecurityFilterChain 구성하기 - 전체 인증 흐름
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService,
            JsonMapper jsonMapper
    ) throws Exception {

        // LoginAuthenticationFilter 생성
        LoginAuthenticationFilter loginAuthenticationFilter =
                new LoginAuthenticationFilter(
                        authenticationManager,
                        jwtUtil,
                        jsonMapper
                );
        // JwtAuthenticationFilter 생성
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(
                        jwtUtil,
                        userDetailsService
                );

        http.   // JWT 방식이므로 STATELESS
                sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 인증 정보 없이 보호된 URL에 접근하면 401 (로그인했지만 역할이 다른경우 403)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                //url별 접근권한
                .authorizeHttpRequests(auth -> auth
                        //에러 응답용
                        .requestMatchers("/error").permitAll()
                        // 회원가입 / 로그인 / Access Token 재발급
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/signup",
                                "/api/users/login",
                                "/api/users/refresh"
                        ).permitAll()

                        // 메뉴 목록 / 단건 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/menus",
                                "/api/menus/*"
                        ).permitAll()

                        // 메뉴 등록
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/menus"
                        ).hasRole("OWNER")

                        // 메뉴 수정
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/menus/*"
                        ).hasRole("OWNER")

                        //단일 메뉴 삭제
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/menus/*"
                        ).hasRole("OWNER")

                        // 주문 생성
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders"
                        ).hasRole("CUSTOMER")

                        // 주문 목록 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders"
                        ).authenticated()

                        // 주문 취소
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/orders/*/cancel"
                        ).hasRole("CUSTOMER")

                        // 주문 상태 변경
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/orders/*/status"
                        ).hasRole("OWNER")

                        // 결제
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders/*/payments"
                        ).hasRole("CUSTOMER")

                        // 나머지 url 로그인 필
                        .anyRequest().authenticated()
                )
                // 필터 순서 : HTTP Request → JwtAuthenticationFilter → LoginAuthenticationFilter
                // → 나머지 Spring Security Filter → 권한 검사 → DispatcherServlet → Controller
                .addFilterBefore( jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class )
                .addFilterAt( loginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class );

        return http.build();
    }


}
