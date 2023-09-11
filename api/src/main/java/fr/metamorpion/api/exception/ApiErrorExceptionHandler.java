package fr.metamorpion.api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@RestController
public class ApiErrorExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FunctionalException.class)
    public final ResponseEntity<ApiError> handleFunctionalError(FunctionalException ex) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                ex.getClientMessage(),
                ex.toString(),
                ex.getHttpStatus(),
                ex.getHttpStatus().value()
        );
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(apiError);
    }

}
