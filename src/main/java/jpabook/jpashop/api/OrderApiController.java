package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * order정보와 orderItem 정보를 함께 출력
     * V1은 엔티티를 직접 노출하는 방법이므로 가급적 피해야한다.
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // LAZY 강제초기화
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // LAZY 강제 초기화
//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();
//            }
        }
        return all;
    }

    /**
     * V2. DTO 사용
     * 너무 많은 쿼리가 실행된다.
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        // orders를 orderDto로 변환
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    /**
     * V3 패치조인 최적화
     * 쿼리 1번으로 같은 결과를 얻을 수 있다.
     * 단점 : 컬렉션을 패치조인하면 페이징을 할 수 없다. 모든 데이터를 DB에서 읽어오고 메모리에서 페이징하기 때문이다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    /**
     * V3.1 컬렉션 페이징 한계 돌파
     * yml파일 : default_batch_fetch_size 추가
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page (
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // ToOne관계는 패치조인 (페이지 영향x)

        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o)).collect(toList());

        return result;
    }

    /**
     * V4. JPA에서 DTO 직접 조회 : 컬렉션 조회 최적화
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * V5. JPA에서 DTO 직접 조회 : V4에서의 쿼리 N+1 문제 개선
     * 쿼리문 2번으로 해결.
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimiztion();
    }


//    // 데이터 중복
//    @GetMapping("/api/v6/orders")
//    public List<OrderFlatDto> ordersV6() {
//        return orderQueryRepository.findAllByDto_flat();
//    }
    /**
     * V6. JPA에서 DTO 직접 조회ㅡ 플랫 데이터 최적화 : 쿼리문 1번으로 해결 (V5 최적화)
     * 쿼리는 한번이지만 DB에서 애플리케이션에 전달하는 데이터에 중복데이터가 있어서 V5보다 느릴 수 있다.
     * 애플리케이션에서 추가 작업을 해야한다.
     * order를 기준으로 페이징을 하면 데이터 중복으로 인해 페이징이 불가능하다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(),
                                o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
                .entrySet()
                .stream().map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(),
                        e.getKey().getOrderDate(),
                        e.getKey().getOrderStatus(),
                        e.getKey().getAddress(),
                        e.getValue()))
                .collect(toList());

    }

    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());

            /*
                order.getOrderItems().stream().forEach(o -> o.getItem().getName());
                orderItems = order.getOrderItems();
                원하는 결과가 나오지만, 결국 orderItems는 엔티티 그대로 노출되기 때문에 적합하지 않다.
                orderItem도 DTO로 변경해주어야한다. (List<OrderItem> X)
            */
        }
    }

    @Getter
    static class OrderItemDto {
        // 상품명, 가격, 개수만 필요한 경우
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
