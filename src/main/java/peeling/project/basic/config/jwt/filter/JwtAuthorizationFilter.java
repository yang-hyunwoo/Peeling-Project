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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.config.jwt.JwtProcess;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.exception.error.ErrorCode;
import peeling.project.basic.property.AesProperty;
import peeling.project.basic.property.JwtProperty;
import peeling.project.basic.repository.MemberRepository;
import peeling.project.basic.util.Aes256Util;
import peeling.project.basic.util.MultiReadHttpServletRequest;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static peeling.project.basic.config.jwt.JwtProcess.createCookieJwt;

/*
 모든 주소에서 동작 (토큰 검증)
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final MemberRepository memberRepository;

    private final boolean localCookie = false; //true : 로컬  false : 쿠키

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

        /*inputStream은 한번만 가능 하기 때문에 실패 시 유저 정보를 가져올수 없어서
          inputStream을 한번 하고 다시 요청 할 때 cache를 이용
         */
        MultiReadHttpServletRequest rereadableRequestWrapper = new MultiReadHttpServletRequest(request);
        chain.doFilter(rereadableRequestWrapper, response);
    }

    private void localVerify(HttpServletRequest request, HttpServletResponse response) {
        if (isHeaderVerify(request)) {
            //토큰이 존재
            String token = request.getHeader(JwtProperty.getHeader()).split(" ")[1].trim();

            try {   //토큰에 아무 이상이 없을 경우
                LoginUser loginUser = JwtProcess.verify(token);
                //임시 세션 (UserDetails 타입 or username) id , role 만 있음
                setAuthentication(loginUser);
            } catch (TokenExpiredException e) {
                //로그인 auto 체크
                if(!Boolean.parseBoolean(request.getHeader("PA_AUT"))) {
                    if(autoChkVerifyExpired(e.getExpiredOn())) {
                        request.setAttribute("exception", "access토큰 만료");
                    }
                }
                //accessToken이 만료가 되었다면 client에서 refreshToken을 받아와
                String refreshToken = request.getHeader("REFRESH_TOKEN").split(" ")[1].trim();
                refreshTokenNullChk(request, response, refreshToken);
            } catch (JWTDecodeException e){
                //doesn't have a valid JSON format
                //JwtDecode 시 exception
                e.printStackTrace();
                request.setAttribute("exception","decode 안되는 토큰");
            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                request.setAttribute("exception","알고리즘 관련 오류");
            } catch (CustomApiException e) {
                e.printStackTrace();
                request.setAttribute("exception",e.getMessage());
            }
        }
    }

    private void cookieVerify(HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.hasText(isCookieVerify(request,"PA_T")) && StringUtils.hasText(isCookieVerify(request,"PA_AUT"))) {
            //토큰이 존재
            String token = isCookieVerify(request , "PA_T");
            try {   //토큰에 아무 이상이 없을 경우
                LoginUser loginUser = JwtProcess.verify(token);
                //임시 세션 (UserDetails 타입 or username) id , role 만 있음
                setAuthentication(loginUser);
            } catch (TokenExpiredException e) {
                if(!Boolean.parseBoolean(isCookieVerify(request, "PA_AUT"))) {
                    if(autoChkVerifyExpired(e.getExpiredOn())) {
                        request.setAttribute("exception", "access토큰 만료");
                    }
                }
                //accessToken이 만료가 되었다면 client에서 refreshToken을 받아와
                String refreshToken = isCookieVerify(request, "PR_T");
                refreshTokenNullChk(request, response, refreshToken);
            } catch (JWTDecodeException e){
                //doesn't have a valid JSON format
                //JwtDecode 시 exception
                e.printStackTrace();
                request.setAttribute("exception","decode 안되는 토큰");
            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                request.setAttribute("exception","알고리즘 관련 오류");
            } catch (CustomApiException e) {
                e.printStackTrace();
                request.setAttribute("exception",e.getMessage());
            }
        }
    }

    private void accessTokenGenerated(HttpServletResponse response, Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomApiException(ErrorCode.LG_MEMBER_INVALIED.getMessage()));
        String accessToken = JwtProcess.create(new LoginUser(member));
        String token = accessToken.split(" ")[1].trim();
        if(localCookie) {
            response.addHeader(JwtProperty.getHeader(), token); //header
        } else {
            response.addHeader("Set-cookie", createCookieJwt(accessToken, "PA_T").toString());
        }
        setAuthentication(JwtProcess.verify(token));
    }

    private void refreshTokenGenerated(HttpServletResponse response, Long userId ,boolean dbInsert) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomApiException(ErrorCode.LG_MEMBER_INVALIED.getMessage()));
        String newRefreshToken = JwtProcess.refresh(new LoginUser(member));
        if(localCookie) {
            response.addHeader("REFRESH_TOKEN", newRefreshToken); //header
        } else {
            response.addHeader("Set-cookie", createCookieJwt(newRefreshToken, "PR_T").toString());
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

    private boolean isHeaderVerify(HttpServletRequest request) {
        String header = request.getHeader(JwtProperty.getHeader());
        String autoChk = request.getHeader("PA_AUT");

        return (header != null && header.startsWith(JwtProperty.getTokenPrefix())) && (autoChk != null);
    }

    private String isCookieVerify(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        String cookieValue = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    cookieValue = cookie.getValue();
                }
            }
        }
        return cookieValue;
    }

    public static boolean autoChkVerifyExpired(Instant expireDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refreshExpired = expireDate.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return ChronoUnit.DAYS.between(refreshExpired, now) <= -1;
    }

    private void refreshTokenNullChk(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
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
            }catch (CustomApiException e3) {
                e3.printStackTrace();
                request.setAttribute("exception",e3.getMessage());
            }
        }
    }
}
