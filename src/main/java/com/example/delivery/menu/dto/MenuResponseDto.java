package com.example.delivery.menu.dto;

import com.example.delivery.menu.entity.Menu;
import lombok.Getter;

/**
 * 메뉴 하나를 조회해서 응답할 때 쓰는 단일 메뉴 응답 DTO
 * 전체 목록은 단일 메뉴 응답 DTO를 List로 담아 응답 예정
 * 생성된 시점에 응답데이터 전달하는객체로 값이 바뀔필요 없어서 final로 필드구성
 */
@Getter
public class MenuResponseDto {

    private final Long menuId;
    private final String food;
    private final Long price;
    private final String menuDetail;

    public MenuResponseDto(Menu menu) {
        this.menuId = menu.getMenuId();
        this.food = menu.getFood();
        this.price = menu.getPrice();
        this.menuDetail = menu.getMenuDetail();
    }
}
