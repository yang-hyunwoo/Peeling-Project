package peeling.project.basic.config.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.domain.constant.MemberEnum;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.util.Aes256Util;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtProcess {

    //토큰 생성
    public static String create(LoginUser loginUser) {
        Aes256Util aes256 = new Aes256Util();
        String id = aes256.encrypt(loginUser.getMember().getId().toString());
        String role = aes256.encrypt(loginUser.getMember().getRole().name());
        String jwtToken = JWT.create()
                .withSubject("bank")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtVO.EXPIRATION_TIME))
                .withClaim("id", id)
                .withClaim("role", role)
                .sign(Algorithm.HMAC512(JwtVO.SECRET));
        return "Bearer " + jwtToken;
    }

    //refresh 토큰 생성
    public static String refresh(LoginUser loginUser) {
        Aes256Util aes256 = new Aes256Util();
        String id = aes256.encrypt(loginUser.getMember().getId().toString());
        String uuid = aes256.encrypt(UUID.randomUUID().toString());
        String jwtToken = JWT.create()
                .withSubject("bank")
                .withExpiresAt(new Date(System.currentTimeMillis() +JwtVO.EXPIRATION_TIME*2))
                .withClaim("id", id)
                .withClaim("refreshToken", uuid)
                .sign(Algorithm.HMAC512(JwtVO.SECRET));
        return jwtToken;
    }

    //토큰 검증 (return 되는 LoginUser 객체를 강제로 시큐리티 세션에 직접 주입)
    public static LoginUser verify(String token)  {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        Aes256Util aes256 = new Aes256Util();
        Long id = Long.parseLong(aes256.decrypt(decodedJWT.getClaim("id").asString()));
        String role = aes256.decrypt(decodedJWT.getClaim("role").asString());
        Member member = Member.builder().id(id).role(MemberEnum.valueOf(role)).build();
        LoginUser loginUser = new LoginUser(member);
        return loginUser;
    }

    public static Long verifyRefresh(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        Aes256Util aes256 = new Aes256Util();
        return  Long.parseLong(aes256.decrypt(decodedJWT.getClaim("id").asString()));
    }

    public static boolean verifyExpired(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refreshExpired = decodedJWT.getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if(now.compareTo(refreshExpired) <=1) {
            return true;
        }
        return false;

    }


}
