package jpabook.jpashop.Controller;

import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) { // 컨트롤러에서 뷰로 넘어갈때 model에 데이터를 넣어서 보냄
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm memberForm, BindingResult result) {

        if (result.hasErrors()) { //result에 오류가 담겨서 실행
            return "members/createMemberForm";
        }
        Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode());
        Member member = new Member();
        member.setName(memberForm.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers(); // 실무에서는 화면에 꼭 필요한 데이터를 가진 DTO를 사용하는 것을 권장.
        model.addAttribute("members", members);

        return "members/memberList";
    }
}
