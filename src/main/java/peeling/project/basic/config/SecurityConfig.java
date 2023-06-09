package peeling.project.basic.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import peeling.project.basic.config.jwt.filter.JwtAuthenticationFilter;
import peeling.project.basic.config.jwt.filter.JwtAuthorizationFilter;
import peeling.project.basic.oauth.OAuth2AuthenticationFailureHandler;
import peeling.project.basic.oauth.OAuth2AuthenticationSuccessHandler;
import peeling.project.basic.oauth.PrincipalOauth2UserService;
import peeling.project.basic.repository.MemberRepository;
import peeling.project.basic.service.MemberService;
import peeling.project.basic.util.CustomAccessDeniedHandler;
import peeling.project.basic.util.CustomAuthenticationEntryPoint;
import peeling.project.basic.util.CustomLogOutHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsConfig corsConfig;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManager authenticationManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));

        return http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) //iframe을 허용하지 않음 .disable() lamda
                .csrf(AbstractHttpConfigurer::disable) //enalbed 일 경우 post맨이 작동하지 않음 .disable() lamda
                .cors(cors -> cors.configurationSource(corsConfig.configurationSource()))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //자바의 세션을 사용하지 않겠다.
                .formLogin(AbstractHttpConfigurer::disable) //시큐리티의 폼로그인을 사용하지 않겠다 .disable() lamda
                .oauth2Login(oauth2Login -> oauth2Login.authorizationEndpoint(authorizationEndpoint-> authorizationEndpoint
                        .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirectionEndpoint-> redirectionEndpoint.baseUri("/login/oauth2/code/**"))
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(principalOauth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler))
                .httpBasic(AbstractHttpConfigurer::disable) //브라우저가 팝업창을 이용하여 사용자 인증을 진행하지 않겠다. .disable() lamda
                .logout(logout-> logout.logoutUrl("/api/logout").logoutSuccessHandler(new CustomLogOutHandler())
                        .deleteCookies("PA_T")
                        .deleteCookies("PR_T")
                        .deleteCookies("PA_AUT"))
                .addFilter(new JwtAuthenticationFilter(authenticationManager,memberService))
                .addFilterBefore(new JwtAuthorizationFilter(authenticationManager, memberRepository), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(new CustomAccessDeniedHandler()))
                .authorizeHttpRequests(authorizeHttpRequests ->                 // /api/** 에 접근 시 권한이 없다면 접근을 불가능하게 하겠다.
                        authorizeHttpRequests
                                .requestMatchers("/api/user/**")
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

    //시큐리티 적용하지 않을 path
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/images/**", "/js/**");
    }
}
