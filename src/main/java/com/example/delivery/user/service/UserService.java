package com.example.delivery.user.service;

import com.example.delivery.global.security.jwt.JwtUtil;
import com.example.delivery.user.dto.SignupRequestDto;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * AS-IS : Refresh Token 검증 후 내용추출하여 새로운 ACCESS TOKEN 재발급 까지 구현
 * TO-BE : Redis기반 Refresh Session 관리, Refresh Token Rotation, 재사용 탐지 구조 활용하여 보안강화예정
 *
 * Refresh Token 보관소로 Redis를 선택한 이유
 * 1. TTL(Time To Live)을 활용한 토큰 만료 자동 처리로 보안성 및 이력 관리 단순화
 * 2. In-Memory(인메모리) 기반의 높은 throughput으로 토큰 재발급 시 메인 DB 부하 차단
 * 3. Key-Value 구조의 효율적인 조회 성능 때문입니다. 인메모리 특성상 데이터 유실 위험이 있지만,
 * 백업 옵션(AOF/RDB) 활용 및 토큰 유실 시 재로그인 유도라는 수용 가능한 트레이드오프 내에서 최선이라 생각하였습니다. *
 */
@Service
public class UserService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            JwtUtil jwtUtil,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Refresh Token 존재하면 새 Access Token 발급
    public String reissueAccessToken(String refreshToken) {

        // Refresh Token 검증 후 Claims 추출
        Claims claims =
                jwtUtil.parseRefreshToken(refreshToken);

        // Refresh Token의 subject에서 loginId 추출
        String loginId = claims.getSubject();

        // 현재 DB의 User 정보 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );

        // 현재 User의 loginId와 role을 기준으로 새 Access Token 생성
        return jwtUtil.createAccessToken(
                user.getLoginId(),
                user.getRole()
        );
    }

    // 회원가입
    @Transactional
    public void signup(SignupRequestDto request) {

        // loginId 중복 검사
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Login ID already exists."
            );
        }

        // 평문 비밀번호를 BCrypt 단방향 해시로 변환
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // 회원가입 정보로 User Entity 생성
        User user = new User(
                request.getUserName(),
                request.getLoginId(),
                encodedPassword,
                request.getRole()
        );

        // DB 저장
        userRepository.save(user);
    }

}
