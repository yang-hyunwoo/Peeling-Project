package peeling.project.basic.dto.response.member;

import lombok.Data;
import peeling.project.basic.domain.member.Member;

@Data
public class JoinMemberResDto {

    private Long id;

    private String username;

    private String fullname;


    public JoinMemberResDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.fullname = member.getFullname();
    }
}
