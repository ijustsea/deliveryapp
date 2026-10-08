package com.example.delivery.global.security.jwt;

import com.example.delivery.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final JwtParser accessParser;
    private final JwtParser refreshParser;

    // 서명키, access/refresh 만료시간 및 각 토큰 전용 검사기 parser 생성
    public JwtUtil(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.accessParser = Jwts.parser().verifyWith(key).require(TYPE_CLAIM, ACCESS).build();
        this.refreshParser = Jwts.parser().verifyWith(key).require(TYPE_CLAIM, REFRESH).build();
    }

    // access 토큰 생성
    public String createAccessToken(String loginId, UserRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(loginId)
                .claim(TYPE_CLAIM, ACCESS)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(key)
                .compact();
    }

    // refresh 토큰 생성
    public String createRefreshToken(String loginId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(loginId)
                .claim(TYPE_CLAIM, REFRESH)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    // 전용 파서로 Access Token을 검증하고 Claims 반환
    public Claims parseAccessToken(String token) {
        return accessParser.parseSignedClaims(token).getPayload();
    }

    // 전용 파서로 refresh Token을 검증하고 Claims 반환
    public Claims parseRefreshToken(String token) {
        return refreshParser.parseSignedClaims(token).getPayload();
    }
}