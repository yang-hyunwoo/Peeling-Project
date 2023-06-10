package peeling.project.basic.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class Response<T> {

    private String resultCode;

    private int httpCode;
    private  T data;


    public static Response<String> error(String resultCode, int httpCode, String data) {
        return new Response<>(resultCode, httpCode, data);
    }

    public static <T> Response<T> successRead(T data){
        return new Response<>("SUCCESS", HttpStatus.OK.value(),data);
    }

    public static <T> Response<T> successNew(T data){
        return new Response<>("SUCCESS", HttpStatus.CREATED.value(),data);
    }

    @Override
    public String toString() {
        return "Response{" +
                "resultCode='" + resultCode + '\'' +
                ", result=" + data +
                '}';
    }

}
