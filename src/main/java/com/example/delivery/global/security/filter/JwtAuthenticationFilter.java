package com.example.delivery.global.security.filter;

import com.example.delivery.global.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // UserDetailsService 타입으로 UserDetailsServiceImpl service Bean 주입
    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // 요청의 Access Token 확인후 인증 정보 SecurityContext에 저장
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        //Authorization Header에서 Access Token만 추출
        String token = extractAccessToken(request);

        // Access Token이 존재하고 아직 인증 정보가 없는 경우에만 인증 처리
        if (StringUtils.hasText(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // Access Token 검증 후 Claims 추출
                Claims claims = jwtUtil.parseAccessToken(token);

                // JWT의 subject에서 loginId 추출
                String loginId = claims.getSubject();

                // loginId를 이용해 사용자 인증 정보 생성
                setAuthentication(loginId);

            } catch (JwtException
                     | AuthenticationException
                     | IllegalArgumentException e) {

                // JWT 검증 또는 사용자 인증 정보 조회 실패 시 인증 정보 제거
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    // request의 Authorization Header에서 Bearer 접두사를 제거하고 JWT 추출 메소드
    private String extractAccessToken(HttpServletRequest request) {

        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);
        // Header가 존재하고 "Bearer "로 시작하는지 확인
        if (StringUtils.hasText(authorization)
                && authorization.startsWith(BEARER_PREFIX)) {

            // "Bearer " 부분을 제거하고 실제 JWT 문자열만 반환
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // loginId로 UserDetails를 조회하고 인증 정보를 SecurityContext에 저장
    private void setAuthentication(String loginId) {

        // 실제로는 UserDetailsServiceImpl의 loadUserByUsername() 실행
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(loginId);

        // 이미 JWT 검증을 통과했으므로 인증된 Authentication 객체 생성
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // 현재 요청에서 사용할 새로운 SecurityContext 생성
        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        // Authentication을 SecurityContext에 저장
        context.setAuthentication(authentication);

        // 완성된 SecurityContext를 현재 요청에 등록
        SecurityContextHolder.setContext(context);
    }
}
