package peeling.project.basic.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.config.jwt.JwtProcess;
import peeling.project.basic.property.AesProperty;
import peeling.project.basic.util.Aes256Util;

import java.io.IOException;

import static peeling.project.basic.config.jwt.JwtProcess.CreateCookie;
import static peeling.project.basic.config.jwt.JwtProcess.CreateCookieJwt;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {




    //JwtAuthenticationFilter와 동일 함함
   @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String accessToken = JwtProcess.create(loginUser);
        String refreshToken = JwtProcess.refresh(loginUser);

       Aes256Util aes256 = new Aes256Util();
       String encrypt = aes256.encrypt(AesProperty.getAesBody(), "true");

        response.addHeader("Set-cookie", CreateCookieJwt(accessToken, "PA_T").toString());
        response.addHeader("Set-cookie", CreateCookieJwt(refreshToken, "PR_T").toString());
       response.addHeader("Set-cookie", CreateCookie(encrypt, "PA_AUT").toString());

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        if(loginUser.getMember().getProvider().equals("naver")) {
            return UriComponentsBuilder.fromUriString("http://localhost:4000/NaverLoginCallback")
                    .build().toUriString();
        } else {
            return UriComponentsBuilder.fromUriString("http://localhost:4000")
                    .build().toUriString();
        }
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
    }

}
