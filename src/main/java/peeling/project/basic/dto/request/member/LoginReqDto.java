package peeling.project.basic.dto.request.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReqDto {

    private String email;

    private String password;

    private String chk;
}
