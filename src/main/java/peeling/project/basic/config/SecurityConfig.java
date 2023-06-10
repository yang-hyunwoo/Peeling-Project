package peeling.project.basic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) //iframe을 허용하지 않음
                .csrf(csrf -> csrf.disable()) //enalbed 일 경우 post맨이 작동하지 않음
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //자바의 세션을 사용하지 않겠다.
                .formLogin(formLogin -> formLogin.disable()) //시큐리티의 폼로그인을 사용하지 않겠다
                .httpBasic(httpBasic -> httpBasic.disable()) //브라우저가 팝업창을 이용하여 사용자 인증을 진행하지 않겠다.
                .authorizeHttpRequests(authorizeHttpRequests ->                 // /api/** 에 접근 시 권한이 없다면 접근을 불가능하게 하겠다.
                        authorizeHttpRequests
                                .requestMatchers("/api/**")
                                .authenticated())
                .authorizeHttpRequests(authorizeHttpRequests ->                 // api/admin/** 에 접근 시 권한이 ADMIN이 아니라면 접근을 불가능하게 하겠다.
                        authorizeHttpRequests
                                .requestMatchers("/api/admin/**")
                                .hasRole("ADMIN"))
                .authorizeHttpRequests(authorizeHttpRequests ->                 //그 외의 URL에 관한 접근은 모두 허용 하겠다.
                        authorizeHttpRequests
                                .anyRequest()
                                .permitAll()).build();
    }

}
