package jpabook.jpashop.api;

import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController // Controller + ResponseBody
@RequiredArgsConstructor
public class MameberApiController {
    private final MemberService memberService;

    // 회원 전체 조회 API
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
        /* 문제점
            if 이대로 실행하면 회원정보만을 원하는 경우에도 orders까지 포함될 것이다.
            엔티티가 변경되면 API스펙이 변한다.
           엔티티를 직접 노출하게 되면 엔티티에 대한 모든 필드가 노출된다.
           @JsonIgnore 를 사용하면 해당 필드에 대한 값은 노출되지 않는다.
           그러나 Member.class 를 사용하는 곳이 많기 때문에 엔티티에 직접적으로 주지말고 DTO를 사용해야한다.
           + 스펙 확장 관련 문제
         */
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count; // 이렇게 count를 추가해도 쉽게 추가 가능. (API 스펙 확장)
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) { //Json으로 온 Body를 Member에 값을 주입
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // DTO 사용으로 Member Entity 필드명을 변경해도 API 스펙이 바뀌지 않는다.
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) { //DTO 사용
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 회원 이름 수정 API
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberResponse(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
