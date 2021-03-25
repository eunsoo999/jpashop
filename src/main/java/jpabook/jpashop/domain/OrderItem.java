package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "order_item")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;
    private int count;

    //protected OrderItem() {
        /* 생성메소드 사용할 때, 누군가는 createOrderItem()를 사용하는데 누군가는 setter를 사용할 수도 있다.
            이렇게 되면 나중에 유지보수하기가 어려워짐.
            그걸 막기위해서 기본생성자의 접근제어자를 protected(상속 관계가 없는 다른 패키지 클래스를 차단)로 해주면 new OrderItem()이 불가능하다.
            상단 @NoArgsConstructor(access = AccessLevel.PROTECTED)으로 줄일 수 있음. (lombok)
         */
    //}

    //==생성 메소드==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        // Item에 가격이 있지만, 할인 등으로 가격이 변동될 수 있기 때문에 orderPirce파라미터를 따로 줌
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);

        return orderItem;
    }

    //==비즈니스 로직==//
    public void cancel() {
        getItem().addStock(count); // 주문했던 수량에 대해 다시 재고에 넣어줌
    }

    //==조회 로직==//
    /**
     * 주문상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
