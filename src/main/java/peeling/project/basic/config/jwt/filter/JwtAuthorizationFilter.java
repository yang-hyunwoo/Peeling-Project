package peeling.project.basic.config.jwt.filter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.config.jwt.JwtProcess;
import peeling.project.basic.config.jwt.JwtVO;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.repository.MemberRepository;

import java.io.IOException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Optional;

/*
 모든 주소에서 동작 (토큰 검증)

 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final MemberRepository memberRepository;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isHeaderVerify(request, response)) {
            //토큰이 존재
            String token = request.getHeader(JwtVO.HEADER).split(" ")[1].trim();

            try {   //토큰에 아무 이상이 없을 경우
                LoginUser loginUser = JwtProcess.verify(token);
                //임시 세션 (UserDetails 타입 or username) id , role 만 있음
                setAuthentication(loginUser);
            } catch (TokenExpiredException e) {

                //accessToken이 만료가 되었다면 client에서 refreshToken을 받아와
                String refreshToken = request.getHeader("REFRESH_TOKEN").trim();
                if(refreshToken==null) {
                    request.setAttribute("exception","잘못된 토큰");

                } else {
                    try {
                        log.info("사용자 토큰 만료 -> 리프래시 토큰 인증 후 토큰 재 생성");
                        //refresh 토큰이 만료가 되지 않았을 경우
                        JwtProcess.verifyRefresh(refreshToken);
                        refreshTokenGenerated(response, refreshToken , false);

                    } catch (TokenExpiredException e2) {
                        //refresh 토큰이 만료가 되었다면 refresh 토큰 재 생성 및 login 안되게 처리
                        //리프래시 토큰 재 생성
//                        refreshTokenGenerated(response, refreshToken , true);

                        // 로그아웃 시키기
                        request.setAttribute("exception", "refresh토큰 만료");

                    } catch (JWTDecodeException e2) {

                        //doesn't have a valid JSON format
                        //JwtDecode 시 exception
                        e2.printStackTrace();
                        request.setAttribute("exception", "decode 안되는 토큰");

                    } catch (SignatureVerificationException e2) {
                        e2.printStackTrace();
                        request.setAttribute("exception", "알고리즘 관련 오류");

                    } catch (MalformedJwtException e2) {
                        e2.printStackTrace();
                        request.setAttribute("exception", "잘못된 토큰");
                    }
                    //refresh token db 저장 및 jwt 분해 해서 refresh 토큰 조회 후 있으면 access 다시 리턴
                    //        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
                    //                .maxAge(7 * 24 * 60 * 60)
                    //                .path("/")
                    //                .build();
                    //        response.addHeader("Set-cookie", cookie.toString());
                }

            } catch (JWTDecodeException e){
                //doesn't have a valid JSON format
                //JwtDecode 시 exception
                e.printStackTrace();
                request.setAttribute("exception","decode 안되는 토큰");

            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                request.setAttribute("exception","알고리즘 관련 오류");
            } catch (MalformedJwtException e) {
                e.printStackTrace();
                request.setAttribute("exception","잘못된 토큰");
            }

        }
        chain.doFilter(request, response);
    }

    private void refreshTokenGenerated(HttpServletResponse response, String refreshToken , boolean chk) {
        Member member = memberRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new CustomApiException("사용자가 없습니다."));
        LoginUser loginUserMember = new LoginUser(member);
        String accessToken = JwtProcess.create(loginUserMember);
        String token = accessToken.split(" ")[1].trim();
        System.out.println("accessToken:::" + token);
        response.addHeader(JwtVO.HEADER, token); //header
        if(chk) { // 리프래시 토큰 생성
            String newRefreshToken = JwtProcess.refresh(loginUserMember);
            response.addHeader("REFRESH_TOKEN", newRefreshToken); //header
            member.refreshTokenUpdIns(newRefreshToken);
            memberRepository.save(member);
        }
        LoginUser loginUser = JwtProcess.verify(token);
        setAuthentication(loginUser);


    }

    private static void setAuthentication(LoginUser loginUser) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isHeaderVerify(HttpServletRequest request , HttpServletResponse response) {
        String header = request.getHeader(JwtVO.HEADER);

        if(header == null || !header.startsWith("Bearer ")) {
            return false;
        } else {
            return true;
        }
    }
}
