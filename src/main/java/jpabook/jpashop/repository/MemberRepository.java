package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Spring Data JPA
public interface MemberRepository extends JpaRepository<Member, Long> {
    //select m from Member m where m.name = ? JPQL 쿼리 자동으로 실행
    List<Member> findByName(String name);
}
