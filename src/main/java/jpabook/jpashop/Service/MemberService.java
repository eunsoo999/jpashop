package jpabook.jpashop.Service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // JPA의 데이터 변경이나 로직은 트랜잭션 안에서 실행되어야한다.
//@AllArgsConstructor // 생성자 주입을 자동으로 만들어줌.
@RequiredArgsConstructor // final을 가지고 있는 필드만 가지고 생성자를 만들어줌.
public class MemberService {

    private final MemberRepository memberRepository; // 컴파일 시점 체크를 해줌

//    @Autowired //생성자 주입
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /**
     * 회원가입
     */
    @Transactional // 쓰기이므로 readOnly 옵션 주면 안된다.
    public Long join(Member member) {
        validateDuplidateMember(member); //이름 중복 회원 검증
        memberRepository.save(member);

        return member.getId();
    }

    // 이름 중복회원 검증
    public void validateDuplidateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());

        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
        /*
         동시에 '홍길동'이라는 이름으로 가입한 경우 동시에 가입되므로 문제가 생길 수 있다.
         이런 경우, 실무에서는 데이터베이스 내 name을 유니크 제약조건을 거는 것이 안전하다.
         */
    }

    // 회원 전체 조회
    //@Transactional(readOnly = true) 이 옵션은 조회하는 곳에서 성능을 최적화함. (읽기용 모드)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 회원 한 명 조회
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

}
