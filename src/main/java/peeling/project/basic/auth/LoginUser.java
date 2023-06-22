package peeling.project.basic.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import peeling.project.basic.domain.member.Member;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
@Getter
public class LoginUser implements UserDetails {

    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> "ROLE_" + member.getRole());
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    /**
     * 휴면 계정 or 컬럼 생성(true , false) 해도 됨
     * 현재 일자 기준
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return ChronoUnit.DAYS.between(member.getLastAccessDate().toLocalDate(),LocalDate.now()) <=365;
    }

    /**
     * 비밀번호 오류 5회 이상
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return member.getLgnFlrCnt() <=4;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 탈퇴여부
     * @return
     */
    @Override
    public boolean isEnabled() {
        return member.isUsed();
    }
}
