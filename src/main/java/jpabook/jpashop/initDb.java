package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

//조회용 샘플 데이터 입력

/**
 * userA
 * JPA1 BOOK
 * JPA2 BOOK
 *
 * userB
 * SPRING1 BOOK
 * SPRING2 BOOK
 */

@Component
@RequiredArgsConstructor
public class initDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void dbInit1() {
            Member member = createMember("userA", "서울", "32", "1323");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 100, 10000);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 100, 20000);
            em.persist(book2);


            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {
            Member member = createMember("userB", "부산", "555", "5432");
            em.persist(member);

            Book book1 = createBook("SPRING BOOK", 100, 30000);
            em.persist(book1);

            Book book2 = createBook("SPRING BOOK", 100, 40000);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 30000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Member createMember(String name, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

        private Book createBook(String name, int stockQuantity, int price) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setStockQuantity(stockQuantity);
            book1.setPrice(price);
            return book1;
        }
    }
}