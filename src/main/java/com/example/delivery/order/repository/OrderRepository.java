package com.example.delivery.order.repository;

import com.example.delivery.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    //UserUserId 로 찾는 이유 : Order에 연결된 User의 userId 이기 떄문
    //customer가 생성한 본인의 주문 전체조회
    List<Order> findAllByUserUserId(Long userId);

    // owner의 메뉴에서 들어온 주문 전체조회
    List<Order> findAllByMenuUserUserId(Long userId);
}
