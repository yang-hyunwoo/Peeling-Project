package peeling.project.basic.dto.response.member;

import lombok.Getter;
import lombok.Setter;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.util.CustomDateUtil;

@Getter
@Setter
public class LoginResDto {

    private Long id;
    private String username;
    private String createdAt;

    public LoginResDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.createdAt = CustomDateUtil.toStringFormat(member.getCreatedAt());
    }
}
