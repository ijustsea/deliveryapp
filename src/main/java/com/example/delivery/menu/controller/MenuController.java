package com.example.delivery.menu.controller;

import com.example.delivery.global.security.UserDetailsImpl;
import com.example.delivery.menu.dto.MenuCreateRequestDto;
import com.example.delivery.menu.dto.MenuResponseDto;
import com.example.delivery.menu.dto.MenuUpdateRequestDto;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // 메뉴 등록
    @PostMapping
    public ResponseEntity<Void> createMenu(
            @Valid @RequestBody MenuCreateRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long currentUserId = userDetails.getUser().getUserId();
        // Service에서 예외가 발생하면 @Transactional 작업이 롤백되고 예외가 상위로 전파되므로,
        // Controller는 성공 응답만 처리하고 예외 응답은 Spring의 예외 처리 구조에 맡긴다. 나머지 메소드 동일이유
        menuService.createMenu(request, currentUserId);
        // 201 Created + 빈 Body
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 메뉴 전체 조회
    @GetMapping
    public ResponseEntity<List<MenuResponseDto>> getMenus(){
        // 200 OK + 메뉴 목록 반환
        return ResponseEntity.ok(menuService.getMenus());
    }

    // 단일 메뉴 조회
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponseDto> getMenu(@PathVariable Long menuId){
        // 200 OK + 메뉴 하나 반환
        return ResponseEntity.ok(menuService.getMenu(menuId));
    }

    // 메뉴 수정
    @PutMapping("/{menuId}")
    public ResponseEntity<Void> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long currentUserId = userDetails.getUser().getUserId();
        menuService.updateMenu(menuId,currentUserId ,request);
        // 200 OK + 빈 Body
        return ResponseEntity.ok().build();
    }
    // 메뉴 삭제
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long menuId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long currentUserId = userDetails.getUser().getUserId();
        menuService.deleteMenu( menuId, currentUserId );
        // 204 No Content + 빈 Body
        return ResponseEntity.noContent().build();
    }

}
