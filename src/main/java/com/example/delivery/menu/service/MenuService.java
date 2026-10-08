package com.example.delivery.menu.service;

import com.example.delivery.menu.dto.MenuCreateRequestDto;
import com.example.delivery.menu.dto.MenuResponseDto;
import com.example.delivery.menu.dto.MenuUpdateRequestDto;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public MenuService(MenuRepository menuRepository, UserRepository userRepository){
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    //메뉴 등록
    @Transactional
    public void createMenu(
            MenuCreateRequestDto request,
            Long currentUserId
    ){
        // DB에서 현재 userId로 조회후  User 객체 반환
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found."
                        )
                );

        // 메뉴 생성 및 값 초기화
        Menu menu = new Menu(
                user,
                request.getFood(),
                request.getPrice(),
                request.getMenuDetail()
        );
        // 메뉴 DB 등록
        menuRepository.save(menu);

    }

    //메뉴 전체 조회
    @Transactional(readOnly = true)
    public List<MenuResponseDto> getMenus(){
        return menuRepository.findAllByIsDeletedFalse().stream().map(MenuResponseDto::new).toList();
    }
    //단일 메뉴 조회
    @Transactional(readOnly = true)
    public MenuResponseDto getMenu(Long menuId){
        Menu menu = findMenu(menuId);

        return new MenuResponseDto(menu);
    }

    //메뉴 수정 (소유자 검사)
    @Transactional
    public void updateMenu(
            Long menuId,
            Long currentUserId,
            MenuUpdateRequestDto request
    ) {
        Menu menu = findMenu(menuId);

        validateOwner(menu, currentUserId);

        menu.update(
                request.getFood(),
                request.getPrice(),
                request.getMenuDetail()
        );
    }

    //메뉴 삭제 (soft delete)
    @Transactional
    public void deleteMenu(
            Long menuId,
            Long currentUserId
    ) {
        Menu menu = findMenu(menuId);

        validateOwner(menu, currentUserId);

        menu.delete();
    }

    //메뉴 조회 메소드
    private Menu findMenu(Long menuId){
        return menuRepository.findByMenuIdAndIsDeletedFalse(menuId)
                .orElseThrow(()->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Menu not found."
                        )
                );
    }

    //소유자 검사 메소드
    private void validateOwner(Menu menu, Long currentUserId){
        if(!menu.getUser().getUserId().equals(currentUserId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Owner access only."
            );
        }
    }

}
