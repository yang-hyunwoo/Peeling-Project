package peeling.project.basic.controller.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import peeling.project.basic.dto.request.member.JoinMemberReqDto;
import peeling.project.basic.dto.response.member.JoinMemberResDto;
import peeling.project.basic.repository.MemberRepository;
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
}
