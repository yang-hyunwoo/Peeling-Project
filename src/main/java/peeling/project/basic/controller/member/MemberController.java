package peeling.project.basic.controller.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import peeling.project.basic.dto.request.member.JoinMemberReqDto;
import peeling.project.basic.dto.response.member.JoinMemberResDto;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.service.MemberService;
import peeling.project.basic.util.Response;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "회원관련", description = "MemberController")
public class MemberController {

    private final MemberService memberService;

//    @Operation(summary = "사용자 회원 가입", description = "사용자 회원 가입", tags = { "MemberController" })
    @Operation(summary = "사용자 회원 가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = "회원 가입 실패", content = @Content(schema = @Schema(implementation = Response.class)))
    })
    @PostMapping("/join")
    public Response<JoinMemberResDto> join(@RequestBody @Valid JoinMemberReqDto joinReqDto , BindingResult bindingResult) {

        JoinMemberResDto join = memberService.join(joinReqDto);
        return Response.successRead(join);
    }

    @GetMapping("/all")
    public String aaa() {
        return "aaa";
    }

    @GetMapping("/all2")
    public String aaa2() {
        return "aaa2";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
