package peeling.project.basic.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class CustomResponseUtil {

    public static void success(HttpServletResponse response , Object dto ,String msg) {
        try{
            ObjectMapper om = new ObjectMapper();
            Response<String> responseDto = Response.successRead(msg);
            String responseBody = om.writeValueAsString(responseDto);
            response.setContentType("application/json; uft-8");
            response.setStatus(200);
            response.getWriter().println(responseBody);
        }catch (Exception e){
            log.error("서버 파싱 에러");
        }
    }

    public static void fail(HttpServletResponse response , String msg , HttpStatus httpStatus) {
        try{
            ObjectMapper om = new ObjectMapper();
            Response<String> responseDto = Response.error("ERROR",HttpStatus.BAD_REQUEST.value() ,msg);
            String responseBody = om.writeValueAsString(responseDto);
            response.setContentType("application/json; uft-8");
            response.setStatus(httpStatus.value());
            response.getWriter().println(responseBody);
        }catch (Exception e){
            log.error("서버 파싱 에러");
        }
    }
}
