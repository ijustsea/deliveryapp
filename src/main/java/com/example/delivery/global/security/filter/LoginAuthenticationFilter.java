package com.example.delivery.global.security.filter;

import com.example.delivery.global.security.UserDetailsImpl;
import com.example.delivery.global.security.jwt.JwtUtil;
import com.example.delivery.user.dto.LoginRequestDto;
import com.example.delivery.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final JsonMapper jsonMapper;

    public LoginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            JsonMapper jsonMapper
    ){
        super(authenticationManager);
        this.jwtUtil =jwtUtil;
        this.jsonMapper = jsonMapper;

        setFilterProcessesUrl("/api/users/login");
    }

    // 로그인 요청의 ID/PW를 읽어 AuthenticationManager에 인증 요청
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        try {
            //JsonMapper로 HTTP Request Body의 JSON을 LoginRequestDto 객체로 변환
            LoginRequestDto loginRequest = jsonMapper.readValue(request.getInputStream(), LoginRequestDto.class);

            // login 요청 객체가 없거나 loginId/password가 비어있어 잘못된 요청 처리
            if (loginRequest == null
                    || !StringUtils.hasText(loginRequest.getLoginId())
                    || !StringUtils.hasText(loginRequest.getPassword())) {

                throw new AuthenticationServiceException(
                        "Invalid login request body."
                );
            }

            //LoginRequestDto 타입 객체에서 loginId, password 꺼내서 아직 인증되지않은 UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(
                            loginRequest.getLoginId(),
                            loginRequest.getPassword()
                    );
            //AuthenticationManager에게 Spring Security 형식만든 객체를 로그인 인증요청하고 결과반환
            return getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException | JacksonException e) {

            throw new AuthenticationServiceException(
                    "Failed to parse login request body.",
                    e
            );
        }
    }

    //로그인 성공
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException {

        // 인증 결과에서 UserDetailsImpl 추출
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authResult.getPrincipal();

        // UserDetailsImpl에 있는 getUser메소드 user정보 추출
        User user = userDetails.getUser();

        // Access Token 생성
        String accessToken =
                jwtUtil.createAccessToken(
                        user.getLoginId(),
                        user.getRole()
                );

        // Refresh Token 생성
        String refreshToken =
                jwtUtil.createRefreshToken(
                        user.getLoginId()
                );

        // Access Token을 Authorization Header에 전달
        response.setHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + accessToken
        );

        // Refresh Token을 Header에 전달
        response.setHeader(
                "Refresh-Token",
                refreshToken
        );

        // 200 응답
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // 로그인 실패
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException {
        // Request Body 파싱 실패, JSON 형식 오류, 필수 로그인 값 누락 등 요청자체가 잘못된 경우
        if (failed instanceof AuthenticationServiceException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // 401 응답 (loginId 또는 password가 틀리는 경우)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
