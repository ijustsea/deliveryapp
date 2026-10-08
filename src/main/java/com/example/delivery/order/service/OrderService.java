package com.example.delivery.order.service;

import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.OrderCreateRequestDto;
import com.example.delivery.order.dto.OrderResponseDto;
import com.example.delivery.order.dto.OrderStateUpdateRequestDto;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderState;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.entity.UserRole;
import com.example.delivery.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            MenuRepository menuRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }

    // 비지니스 메소드 4개
    // 1.CUSTOMER 주문 생성 (PENDING)
    @Transactional
    public OrderResponseDto createOrder(OrderCreateRequestDto request, Long currentUserId) {
        // 인증된 userId로 기존 User Entity 조회후 담
        User user = findUser(currentUserId);

        //메뉴 조회
        Menu menu = menuRepository.findByMenuIdAndIsDeletedFalse(request.getMenuId())
                    .orElseThrow(()-> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Menu not found"
                    ));

        // 주문 총액 계산 = menu의 price * request의 counts
        Long totalAmount;
        try {
            totalAmount = Math.multiplyExact( menu.getPrice(), (long) request.getCounts());
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order total is too large.",
                    e //원인 보존
            );
        }

        // Order 생성
        Order order = new Order(
                user,
                menu,
                request.getCounts(),
                totalAmount,
                request.getDeliveryAddress()
        );

        // 주문 저장 후 응답 DTO 반환
        Order savedOrder = orderRepository.save(order);

        return new OrderResponseDto(savedOrder);
    }

    // 2.CUSTOMER/OWNER별 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrders(Long currentUserId) {
        User user = findUser(currentUserId);

        List<Order> orders;

        // CUSTOMER → 본인이 생성한 전체 주문 조회
        if (user.getRole() == UserRole.CUSTOMER) {
            orders = orderRepository.findAllByUserUserId(currentUserId);
        }
        // OWNER → 본인 메뉴에 들어온 전체 주문 조회
        else if (user.getRole() == UserRole.OWNER) {
            orders = orderRepository.findAllByMenuUserUserId(currentUserId);
        }
        // 허용되지 않은 역할은 접근 거부
        else {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Order access denied."
            );
        }

        // Entity 목록을 응답 DTO에 하나하나 담고 리스트로 반환
        return orders.stream()
                .map(OrderResponseDto::new)
                .toList();
    }

    // 3.CUSTOMER 주문 취소 (PENDING → CANCELED)
    @Transactional
    public void cancelOrder(Long orderId, Long currentUserId) {
        // 주문 조회 (없으면 404)
        Order order = findOrder(orderId);
        // CUSTOMER 본인 주문인지 검사 (아니면 403)
        validateCustomer(order, currentUserId);

        try {
            // Entity에서 현재 상태가 PENDING인지 검사 후 CANCELED로 변경
            order.cancel();

        } catch (IllegalStateException e) {
            // 현재 상태가 PENDING이 아니면 409
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage(),
                    e
            );
        }
    }

    // 4.OWNER 본인 메뉴의 주문 상태 변경
    // PAID → ACCEPTED / ACCEPTED → COMPLETED
    @Transactional
    public void updateOrderState(Long orderId, Long currentUserId, OrderStateUpdateRequestDto request) {
        // 주문 조회 (없으면 404)
        Order order = findOrder(orderId);
        // OWNER 본인 메뉴의 주문인지 확인 (아니면 403)
        validateOwner(order, currentUserId);

        // DTO에서 변경하려는 목표 상태 추출
        OrderState targetState = request.getOrderState();

        try {
            // 주문 수락
            if (targetState == OrderState.ACCEPTED) {
                order.accept();
            }
            // 배달 완료
            else if (targetState == OrderState.COMPLETED) {
                order.complete();
            }
            // 허용하지 않는 목표 상태
            else {
                throw new IllegalStateException(
                        "Invalid target order state."
                );
            }

        } catch (IllegalStateException e) {
            // 상태 변경 실패 시 409 Conflict
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage(),
                    e
            );
        }
    }

    //공통메소드 4개
    // 1.user 조회 메소드
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));
    }
    // 2.Order 조회 메소드
    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found."
                ));
    }

    // 3.CUSTOMER 본인 주문 확인 메소드
    private void validateCustomer(Order order, Long currentUserId) {
        if (!order.getUser().getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Customer access only."
            );
        }
    }
    // 4.OWNER 본인 메뉴의 주문 확인 메소드
    private void validateOwner(Order order, Long currentUserId) {
        if (!order.getMenu().getUser().getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Owner access only."
            );
        }
    }

}
