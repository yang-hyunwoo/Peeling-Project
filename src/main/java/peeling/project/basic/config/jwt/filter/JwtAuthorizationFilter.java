package peeling.project.basic.config.jwt.filter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.config.jwt.JwtProcess;
import peeling.project.basic.config.jwt.JwtVO;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.repository.MemberRepository;
import java.io.IOException;

/*
 모든 주소에서 동작 (토큰 검증)

 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final MemberRepository memberRepository;

    private boolean localCookie = false;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(localCookie) {
            localVerify(request, response);
        } else {
            cookieVerify(request, response);
        }
        chain.doFilter(request, response);
    }

    private void localVerify(HttpServletRequest request, HttpServletResponse response) {
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
                    request.setAttribute("exception","리프래시 토큰이 없음");

                } else {
                    try {
                        log.info("사용자 토큰 만료 -> 리프래시 토큰 인증 후 토큰 재 생성");
                        //refresh 토큰이 만료가 되지 않았을 경우
                        Long loginId = JwtProcess.verifyRefresh(refreshToken);

                        //새 accessToken 생성
                        accessTokenGenerated(response,  loginId);

                        // 만료일이 하루 남았을 경우 refreshToken 재생성 마지막 파라미터 true : dbInsert false: dbInsert X

                        if(JwtProcess.verifyExpired(refreshToken)) {
                            refreshTokenGenerated(response,  loginId , false);
                        }

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

                    }
                    //refresh token db 저장 및 jwt 분해 해서 refresh 토큰 조회 후 있으면 access 다시 리턴

                }

            } catch (JWTDecodeException e){
                //doesn't have a valid JSON format
                //JwtDecode 시 exception
                e.printStackTrace();
                request.setAttribute("exception","decode 안되는 토큰");

            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                request.setAttribute("exception","알고리즘 관련 오류");
            }
        }
    }


    private void cookieVerify(HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.hasText(isCookieVerify(request, response,"PA_T"))) {
            //토큰이 존재
            String token = isCookieVerify(request, response , "PA_T");

            try {   //토큰에 아무 이상이 없을 경우
                LoginUser loginUser = JwtProcess.verify(token);
                //임시 세션 (UserDetails 타입 or username) id , role 만 있음
                setAuthentication(loginUser);
            } catch (TokenExpiredException e) {

                //accessToken이 만료가 되었다면 client에서 refreshToken을 받아와
                String refreshToken = isCookieVerify(request, response, "PR_T");
                if(StringUtils.hasText(refreshToken)) {
                    request.setAttribute("exception","리프래시 토큰이 없음");

                } else {
                    try {
                        log.info("사용자 토큰 만료 -> 리프래시 토큰 인증 후 토큰 재 생성");
                        //refresh 토큰이 만료가 되지 않았을 경우
                        Long loginId = JwtProcess.verifyRefresh(refreshToken);

                        //새 accessToken 생성
                        accessTokenGenerated(response,  loginId);

                        // 만료일이 하루 남았을 경우 refreshToken 재생성 마지막 파라미터 true : dbInsert false: dbInsert X
                        if(JwtProcess.verifyExpired(refreshToken)) {
                            refreshTokenGenerated(response,  loginId , false);
                        }

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

                    }
                    //refresh token db 저장 및 jwt 분해 해서 refresh 토큰 조회 후 있으면 access 다시 리턴

                }

            } catch (JWTDecodeException e){
                //doesn't have a valid JSON format
                //JwtDecode 시 exception
                e.printStackTrace();
                request.setAttribute("exception","decode 안되는 토큰");

            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                request.setAttribute("exception","알고리즘 관련 오류");
            }
        }
    }

    private void accessTokenGenerated(HttpServletResponse response, Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomApiException("사용자가 없습니다."));
        String accessToken = JwtProcess.create(new LoginUser(member));
        String token = accessToken.split(" ")[1].trim();
        if(localCookie) {
            response.addHeader(JwtVO.HEADER, token); //header
        } else {
            ResponseCookie cookie = ResponseCookie.from("PA_T", accessToken.split(" ")[1].trim())
                    .maxAge(7 * 24 * 60 * 60)
//                    .httpOnly(true)
//                    .secure(true)
                    .path("/")
                    .build();
            response.addHeader("Set-cookie", cookie.toString());
        }
        setAuthentication(JwtProcess.verify(token));
    }

    private void refreshTokenGenerated(HttpServletResponse response, Long userId ,boolean dbInsert) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomApiException("사용자가 없습니다."));
        String newRefreshToken = JwtProcess.refresh(new LoginUser(member));
        if(localCookie) {
            response.addHeader("REFRESH_TOKEN", newRefreshToken); //header
        } else {
            ResponseCookie refreshCookie = ResponseCookie.from("PR_T", newRefreshToken)
                    .maxAge(7 * 24 * 60 * 60)
//                    .httpOnly(true)
//                    .secure(true)
                    .path("/")
                    .build();
            response.addHeader("Set-cookie", refreshCookie.toString());
        }
        if(dbInsert) {
            member.refreshTokenUpdIns(newRefreshToken);
            memberRepository.save(member);
        }
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

    private String isCookieVerify(HttpServletRequest request , HttpServletResponse response , String cookieName) {
        Cookie[] cookies = request.getCookies();
        String cookieValue = null;
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    cookieValue = cookie.getValue();
                }
            }
        }
        return cookieValue;
    }

}
