package peeling.project.basic.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberEnum {

    SUPER("루트") , ADMIN("관리자") , USER("사용자");

    private String value;

}
