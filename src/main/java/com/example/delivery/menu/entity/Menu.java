package com.example.delivery.menu.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="menu_id")
    private Long menuId;

    //사장님이 메뉴 여러개 가질수 있어서  menu가 외래키 주인 N
    @ManyToOne(fetch = FetchType.LAZY) //불필요한 조회 Lazy
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(name = "food", nullable = false, length = 100)
    private String food;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "menu_detail", length = 255)
    private String menuDetail;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    // 메뉴 등록 시 entity 초기화에 사용하는 생성자
    public Menu(
            User user,
            String food,
            Long price,
            String menuDetail
    ) {
        this.user = user;
        this.food = food;
        this.price = price;
        this.menuDetail = menuDetail;
        this.isDeleted = false;
    }

    // 메뉴 내용 수정용 메소드
    public void update(
            String food,
            Long price,
            String menuDetail
    ){
        this.food = food;
        this.price = price;
        this.menuDetail = menuDetail;
    }

    // 메뉴 삭제용 메소드
    public void delete(){
        this.isDeleted = true;
    }

}
