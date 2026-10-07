package com.example.delivery.user.repository;

import com.example.delivery.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //사용자 확인용 메소드
    Optional<User> findByLoginId(String loginId);
    //로그인 아이디 중복 확인용
    boolean existsByLoginId(String loginId);
}
