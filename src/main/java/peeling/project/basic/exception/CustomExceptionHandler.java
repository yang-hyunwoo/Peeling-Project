package peeling.project.basic.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import peeling.project.basic.util.Response;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(CustomApiException.class)
    public Response<?> apiException(CustomApiException e) {
        log.error(e.getMessage());
        return Response.error("ERROR", HttpStatus.BAD_REQUEST.value(), e.getMessage() );

    }

//    @ExceptionHandler(CustomValidationException.class)
//    public ResponseEntity<?> validationApiException(CustomValidationException e) {
//        log.error(e.getMessage());
//        return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), e.getErrorMap()), HttpStatus.BAD_REQUEST);
//
//    }
//
//    @ExceptionHandler(CustomForbiddenException.class)
//    public ResponseEntity<?> forbiddenException(CustomForbiddenException e) {
//        log.error(e.getMessage());
//        return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), null), HttpStatus.FORBIDDEN);
//
//    }


}
