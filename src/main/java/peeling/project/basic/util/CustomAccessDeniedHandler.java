package peeling.project.basic.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import peeling.project.basic.exception.error.ErrorCode;

import java.io.IOException;



public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ObjectMapper om = new ObjectMapper();
        Response<String> error = Response.error("ERROR", HttpStatus.UNAUTHORIZED.value(), ErrorCode.LG_NO_AUTHORIZATION.getMessage());
        String responseBody = om.writeValueAsString(error);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(401);
        response.getWriter().println(responseBody);
    }
}
