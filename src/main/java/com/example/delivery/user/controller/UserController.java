package com.example.delivery.user.controller;

import com.example.delivery.user.dto.SignupRequestDto;
import com.example.delivery.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Refresh Token으로 새로운 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @RequestHeader("Refresh-Token") String refreshToken
    ) {

        String newAccessToken =
                userService.reissueAccessToken(refreshToken);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + newAccessToken
                )
                .build();
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {

        userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}