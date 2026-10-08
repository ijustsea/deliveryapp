package com.example.delivery.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuUpdateRequestDto {
    @NotBlank
    @Size(min = 1, max = 100)
    private String food;
    @NotNull
    @Positive
    private Long price;

    @Size(max = 255)
    private String menuDetail;
}