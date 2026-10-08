package com.example.delivery.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OWNER가 메뉴등록시 food, price, menuDetail을 받아오는 요청용 DTO.
 */
@Getter
@NoArgsConstructor
public class MenuCreateRequestDto {

    @NotBlank
    @Size(max = 100)
    private String food;

    @NotNull
    @Positive
    private Long price;

    @Size(max = 255)
    private String menuDetail;
}
