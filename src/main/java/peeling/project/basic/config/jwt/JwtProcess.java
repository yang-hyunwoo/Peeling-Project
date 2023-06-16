package peeling.project.basic.config.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.domain.constant.MemberEnum;
import peeling.project.basic.domain.member.Member;

import java.util.Date;
import java.util.UUID;

public class JwtProcess {

    //토큰 생성
    public static String create(LoginUser loginUser) {
        String jwtToken = JWT.create()
                .withSubject("bank")
                .withExpiresAt(new Date(System.currentTimeMillis() +JwtVO.EXPIRATION_TIME))
                .withClaim("id", loginUser.getMember().getId())
                .withClaim("role", loginUser.getMember().getRole().name())
                .sign(Algorithm.HMAC512(JwtVO.SECRET));
        return "Bearer " + jwtToken;
    }

    //refresh 토큰 생성
    public static String refresh(LoginUser loginUser) {
        String uuid = UUID.randomUUID().toString();
        String jwtToken = JWT.create()
                .withSubject("bank")
                .withExpiresAt(new Date(System.currentTimeMillis() +JwtVO.EXPIRATION_TIME*2))
                .withClaim("id", loginUser.getMember().getId())
                .withClaim("refreshToken", uuid)
                .sign(Algorithm.HMAC512(JwtVO.SECRET));
        return jwtToken;
    }

    //토큰 검증 (return 되는 LoginUser 객체를 강제로 시큐리티 세션에 직접 주입)
    public static LoginUser verify(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);

        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
        Member member = Member.builder().id(id).role(MemberEnum.valueOf(role)).build();
        LoginUser loginUser = new LoginUser(member);
        return loginUser;
    }

    public static Long verifyRefresh(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        return decodedJWT.getClaim("id").asLong();
    }
}
