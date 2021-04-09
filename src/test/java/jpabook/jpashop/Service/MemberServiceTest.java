package jpabook.jpashop.Service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest // 스프링 컨테이너 안에서 테스트를 돌림
@Transactional // 데이터를 변경하고 다시 롤백.
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;
    //엔티티매니저를 사용하여 flush를 해서 insert문을 볼 수 있음 이후 바로 (rollback)

    @Test
    //@Rollback(false) // 트랜잭션이 롤백안하고 커밋. 안써주면 롤백.
    public void 회원가입() throws Exception {
        //given 주어지는 것
        Member member = new Member();
        member.setName("kim");

        //when 결과를 실행했을 때
        Long savedId = memberService.join(member);

        //then 결과
        em.flush();
        assertEquals(member, memberRepository.findById(savedId));
        // member와 디비에 저장되어있는 member가 같은지 확인. 같으면 정상적으로 가입이 된 것.
    }

    @Test
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("lee");

        Member member2 = new Member();
        member2.setName("lee");

        //when
        memberService.join(member1);
        try {
            memberService.join(member2); // 예외발생해야함.
        } catch (IllegalStateException e) {
            return;
        }
        //then
        fail("예외가 발생해야한다.");

    }

    /* 위 코드와 해당 코드는 같음.
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외2() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("lee");

        Member member2 = new Member();
        member2.setName("lee");

        //when
        memberService.join(member1);
        memberService.join(member2); // 예외발생해야함.

        //then
        fail("예외가 발생해야한다.");

    }
     */


}