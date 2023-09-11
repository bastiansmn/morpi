package fr.metamorpion.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {
    private LocalDateTime timestamp;
    private String message;
    private String devMessage;
    private HttpStatus httpStatusString;
    private Integer httpStatus;
}
