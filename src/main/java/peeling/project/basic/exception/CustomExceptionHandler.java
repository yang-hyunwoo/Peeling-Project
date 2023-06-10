package peeling.project.basic.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import peeling.project.basic.util.Response;

@RestControllerAdvice
public class CustomExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
