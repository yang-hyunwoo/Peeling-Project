package peeling.project.basic.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 시큐리티로 로그인이 될때 , 시큐리티가 loadUserByUsername() 실행해서 username 체크
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("username::::"+username);
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new InternalAuthenticationServiceException("인증 실패"));
        return new LoginUser(member);
    }
}
