package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    //전체 메뉴 조회
    List<Menu> findAllByIsDeletedFalse();
    //단일 메뉴 조회
    Optional<Menu> findByMenuIdAndIsDeletedFalse(Long menuId);


}