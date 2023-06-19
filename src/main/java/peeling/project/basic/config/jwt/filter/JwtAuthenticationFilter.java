package peeling.project.basic.config.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

import static peeling.project.basic.config.jwt.JwtProcess.CreateCookie;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {



    private AuthenticationManager authenticationManager;

    private MemberRepository memberRepository;

    private boolean localCookie = false; //true : 로컬  false : 쿠키
    @Value("${aes.body}")
    private  String aesKeyBody2;

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
        /**
         * 헤더로 설정 or 쿠키로 설정
         */
        if(localCookie) {
            response.addHeader(JwtVO.HEADER, accessToken);
            response.addHeader("REFRESH_TOKEN", refreshToken);
        } else {
            //쿠키 시간은 동일하게 맞춤 accesstoken에 expired 타임이 있기 때문 ??...;
            response.addHeader("Set-cookie", CreateCookie(accessToken, "PA_T").toString());
            response.addHeader("Set-cookie", CreateCookie(refreshToken, "PR_T").toString());
        }

        boolean dbInsert = false;

        LoginResDto loginRespDto = new LoginResDto(loginUser.getMember());
        Member member = memberRepository.findById(loginUser.getMember().getId()).orElseThrow(() -> new CustomApiException("유저를 찾을수 없습니다"));
        if(dbInsert) {
            member.refreshTokenUpdIns(refreshToken);
            memberRepository.save(member);
        }
        CustomResponseUtil.success(response, loginRespDto,"로그인 성공");

    }
}
