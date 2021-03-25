package jpabook.jpashop.api;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.*;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
/**
 *
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;
    /**
     * V1. 엔티티 직접 노출 (사용해선 안됨)
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * 첫번째 문제
     * - 양방향 관계로 무한루프 발생 -> 양방향 관계에서 한 쪽에 @JsonIgnore를 붙여줘야한다. (Member, Delivery, OrderItem)
     * -> 둘 중 하나는 JsonIgnore로 막아줘야한다.
     *
     * 두번째 문제 Type definition error
     * - Order안에서 지연로딩에 의해 Member가 프록시 객체로 되어있는데 그상태로 Member를 조회함.
     * -> 하이버네이트가 지연로딩인 경우에 무시하도록 할 수 있다. (hibernate5)
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화 order.getMember()까지는 프록시 객체
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    /**
     * V2. DTO 사용
     * Order, Member, Delivery 세 개의 테이블을 조회하므로 실행되는 쿼리가 많다.
     * (N + 1 문제)
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(toList());
        return result;
    }

    /**
     * V3. 패치 조인
     * V2에서 orders 결과가 2개일 때, 쿼리가 총 5번이 나가지면 패치조인을 사용하면 쿼리 1번으로 같은 결과를 반환.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(toList());
        return result;
    }

    /**
     * V4. JPA에서 DTO로 바로 조회
     * 패치조인과 from 이하 절은 같지만, 직접 쿼리를 작성했기떄문에 select에서 원하는 값만 받아온다.
     * V3에 비해 V4는 재사용성이 떨어진다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // 배송지 정보

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화 : 영속성컨텍스트에 값이 없으면 디비에서 가져옴
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }
}