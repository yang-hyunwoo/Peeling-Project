package peeling.project.basic.controller.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import peeling.project.basic.dto.request.member.JoinMemberReqDto;
import peeling.project.basic.dto.response.member.JoinMemberResDto;
import peeling.project.basic.service.MemberService;
import peeling.project.basic.util.Response;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public Response<JoinMemberResDto> join(@RequestBody @Valid JoinMemberReqDto joinReqDto , BindingResult bindingResult) {

        JoinMemberResDto join = memberService.join(joinReqDto);
        return Response.successRead(join);
    }

    @GetMapping("/all")
    public String aaa() {
        return "aaa";
    }
}
