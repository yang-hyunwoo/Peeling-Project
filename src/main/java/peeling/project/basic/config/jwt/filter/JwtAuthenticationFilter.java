package peeling.project.basic.config.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.config.jwt.JwtProcess;
import peeling.project.basic.dto.request.member.LoginReqDto;
import peeling.project.basic.dto.response.member.LoginResDto;
import peeling.project.basic.exception.error.ErrorCode;
import peeling.project.basic.property.AesProperty;
import peeling.project.basic.property.JwtProperty;
import peeling.project.basic.service.MemberService;
import peeling.project.basic.util.Aes256Util;
import peeling.project.basic.util.CustomResponseUtil;

import java.io.IOException;

import static peeling.project.basic.config.jwt.JwtProcess.createCookie;
import static peeling.project.basic.config.jwt.JwtProcess.createCookieJwt;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final MemberService memberService;

    private final boolean localCookie = false; //true : 로컬  false : 쿠키

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, MemberService memberService ) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager = authenticationManager;
        this.memberService = memberService;
    }

    //post /login
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            //강제 로그인  loginReqDto.getUsername() 이게  loadUserByUsername()안의 파라미터로 작동한다.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginReqDto.getEmail(), loginReqDto.getPassword());

            //JWT를 쓴다 하더라도 , 컨트롤러 진입을 하면 시큐리티의 권한체크 , 인증체크의 도움을 받을수 있게 세션을 만듬
            // 세션의 유효기간은 request하고 , response 하면 끝
            return authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            // unsuccessfulAuthentication 호출
            throw new InternalAuthenticationServiceException(e.toString(),e);
        }
    }

    //return authentication 잘 작동하면 successfulAuthentication 해당 메서드 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("디버그 : successfulAuthentication 호출됨");
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String accessToken = JwtProcess.create(loginUser);
        String refreshToken = JwtProcess.refresh(loginUser);
        ObjectMapper om = new ObjectMapper();
        LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

        /**
         * 헤더로 설정 or 쿠키로 설정
         */
        LoginResDto loginRespDto = new LoginResDto(loginUser.getMember());
        memberService.memberLgnFailInit(loginUser.getMember().getId()); // 로그인 실패 횟수 초기화
        if(localCookie) {
            response.addHeader(JwtProperty.getHeader(), accessToken);
            response.addHeader("REFRESH_TOKEN", refreshToken);
            response.addHeader("PA_AUT", loginReqDto.getChk());
        } else {
            //쿠키 시간은 동일하게 맞춤 accesstoken에 expired 타임이 있기 때문 ??...;
            response.addHeader("Set-cookie", createCookieJwt(accessToken, "PA_T").toString());
            response.addHeader("Set-cookie", createCookieJwt(refreshToken, "PR_T").toString());
            response.addHeader("Set-cookie", createCookie(loginReqDto.getChk(), "PA_AUT").toString());
        }

        boolean dbInsert = false;

        if(dbInsert) {
//            member.refreshTokenUpdIns(refreshToken);
//            memberRepository.save(member);
        }
        CustomResponseUtil.success(response, loginRespDto,"로그인 성공");
    }

    //로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        ErrorCode errorCode = unsuccessException(request, failed.getCause());
        CustomResponseUtil.fail(response, errorCode.getMessage(), errorCode.getStatus());
    }

    //request.getParameter("username") 조회할 거로 수정
    public ErrorCode unsuccessException(HttpServletRequest request, Throwable failed) throws IOException {
        ErrorCode errorCode;

        if (failed instanceof BadCredentialsException) {
            //비밀번호가 일치하지 않을 때 던지는 예외
            errorCode = ErrorCode.MEMBER_ID_PW_INVALIED;
            ObjectMapper om = new ObjectMapper();
            memberService.memberLgnFailCnt(om.readValue(request.getInputStream(), LoginReqDto.class).getEmail());//실패 횟수 증가
        } else if (failed instanceof InternalAuthenticationServiceException) {
            //존재하지 않는 아이디일 때 던지는 예외
            errorCode = ErrorCode.MEMBER_ID_PW_INVALIED;
        } else if (failed instanceof LockedException) {
            // 인증 거부 - 잠긴 계정
            errorCode = ErrorCode.PASSWORD_WRONG;
        } else if (failed instanceof AuthenticationCredentialsNotFoundException) {
            // 인증 요구가 거부됐을 때 던지는 예외
            errorCode = ErrorCode.MEMBER_ID_PW_INVALIED;
        } else if (failed instanceof DisabledException) {
            //인증 거부 - 계정 비활성화
            errorCode = ErrorCode.DISABLED_MEMBER;
        } else if (failed instanceof AccountExpiredException) {
            //인증 거부 - 계정 유효기간 만료
            errorCode = ErrorCode.DORMANT_ACCOUNT;
        } else if (failed instanceof CredentialsExpiredException) {
            //인증 거부 - 비밀번호 유효기간 만료 -> vue 화면 이동
            errorCode = ErrorCode.PASSWORD_DATE_OVER;
        } else {
            errorCode = ErrorCode.ANOTHER_ERROR;
        }
        return errorCode;
    }
}
