package peeling.project.basic.dto.request.log;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.domain.log.Log;
import peeling.project.basic.domain.member.Member;


@Getter
@Setter
public class LogReqDto {

    private String uuid;

    private boolean sucesStts;

    private String methodName;

    private String httpMethod;

    private String response;

    private String request;

    private String errorMsg;

    public static LogReqDto of(String uuid,
                     boolean sucesStts,
                     String methodName,
                     String httpMethod,
                     String response,
                     String request,
                     String errorMsg) {
        return new LogReqDto(uuid,
                sucesStts,
                methodName,
                httpMethod,
                response,
                request,
                errorMsg);
    }

    private LogReqDto(String uuid,
                     boolean sucesStts,
                     String methodName,
                     String httpMethod,
                     String response,
                     String request,
                     String errorMsg) {
        this.uuid = uuid;
        this.sucesStts = sucesStts;
        this.methodName = methodName;
        this.httpMethod = httpMethod;
        this.response = response;
        this.request = request;
        this.errorMsg = errorMsg;
    }

    public Log toEntity(LoginUser loginUser) {
        return Log.builder()
                .member(loginUser.getMember())
                .uuid(uuid)
                .sucesStts(sucesStts)
                .methodName(methodName)
                .httpMethod(httpMethod)
                .response(response)
                .request(request)
                .errorMsg(errorMsg)
                .build();
    }

}
