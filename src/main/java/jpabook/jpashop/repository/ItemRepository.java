package jpabook.jpashop.repository;

import jpabook.jpashop.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor // final을 가지고 있는 필드만 가지고 생성자를 만들어줌.
public class ItemRepository {

    private final EntityManager em;

    // 상품 저장
    public void save(Item item) {
        if (item.getId() == null) { // 디비에 넣기 전에는 id가 없으므로 persist
            em.persist(item);
        } else {
            em.merge(item); // merge 사용 (item은 영속성컨텍스트로 들어가는게 아니고, em.merger(item)자체가 영속성컨텍스트에서 관리함.
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
