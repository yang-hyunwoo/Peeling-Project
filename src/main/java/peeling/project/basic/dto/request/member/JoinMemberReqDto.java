package peeling.project.basic.dto.request.member;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import peeling.project.basic.domain.constant.MemberEnum;
import peeling.project.basic.domain.member.Member;

@Getter
@Setter
public class JoinMemberReqDto {

    @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해 주세요.")
    @NotEmpty
    private String username;
    @NotEmpty
    @Size(min = 4, max = 20)
    private String password;
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9]{2,6}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$",message ="이메일 형식으로 작성해 주세요." )
    private String email;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$" , message = "영문/한글 1~20자 이내로 작성해 주세요.")
    private String fullname;

    public Member toEntity(BCryptPasswordEncoder bCryptPasswordEncoder) {
        return Member.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode(password))
                .email(email)
                .fullname(fullname)
                .role(MemberEnum.USER)
                .lgnFlrCnt(0)
                .isUsed(true)
                .build();
    }
}
