package peeling.project.basic.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        //인증이 필요 없는 url 에서는 작동 하지 않는다.
        String exception = (String)request.getAttribute("exception");
        if(exception==null) {
            exception = "로그인을 진행해 주세요.";
        }

        responseWrite(response,exception);
    }

    private static void responseWrite(HttpServletResponse response , String msg) throws IOException {
        ObjectMapper om = new ObjectMapper();
        Response<String> error = Response.error("ERROR", HttpStatus.UNAUTHORIZED.value(), msg);
        String responseBody = om.writeValueAsString(error);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(401);
        response.getWriter().println(responseBody);
    }
}
