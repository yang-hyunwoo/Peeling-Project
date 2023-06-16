package peeling.project.basic.config.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.config.jwt.JwtProcess;
import peeling.project.basic.config.jwt.JwtVO;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.dto.request.member.LoginReqDto;
import peeling.project.basic.dto.response.member.LoginResDto;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.repository.MemberRepository;
import peeling.project.basic.util.CustomResponseUtil;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {



    private AuthenticationManager authenticationManager;

    private MemberRepository memberRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
    }

    //post /login
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            //강제 로그인  loginReqDto.getUsername() 이게  loadUserByUsername()안의 파라미터로 작동한다.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword());

            //JWT를 쓴다 하더라도 , 컨트롤러 진입을 하면 시큐리티의 권한체크 , 인증체크의 도움을 받을수 있게 세션을 만듬
            // 세션의 유효기간은 request하고 , response 하면 끝
            Authentication authentication = authenticationManager.authenticate(authenticationToken); //UserDetailsService의 loadUserByUsername
            return authentication;
        } catch (Exception e) {
            // unsuccessfulAuthentication 호출
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    //로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.fail(response, "ID 및 비밀번호를 확인해 주세요.", HttpStatus.UNAUTHORIZED);
    }

    //return authentication 잘 작동하면 successfulAuthentication 해당 메서드 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("디버그 : successfulAuthentication 호출됨");
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String accessToken = JwtProcess.create(loginUser);
        String refreshToken = JwtProcess.refresh(loginUser);

        //refresh token db 저장 및 jwt 분해 해서 refresh 토큰 조회 후 있으면 access 다시 리턴
//        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
//                .maxAge(7 * 24 * 60 * 60)
//                .path("/")
//                .build();
//        response.addHeader("Set-cookie", cookie.toString());

        response.addHeader(JwtVO.HEADER, accessToken);
        response.addHeader("REFRESH_TOKEN", refreshToken);
        LoginResDto loginRespDto = new LoginResDto(loginUser.getMember());
        Member member = memberRepository.findById(loginUser.getMember().getId()).orElseThrow(() -> new CustomApiException("유저를 찾을수 없습니다"));
        member.refreshTokenUpdIns(refreshToken);
        memberRepository.save(member);
        CustomResponseUtil.success(response, loginRespDto);

    }
}