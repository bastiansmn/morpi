package fr.metamorpion.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FunctionalException extends Exception {

    private final HttpStatus httpStatus;
    private final String clientMessage;

    public FunctionalException(FunctionalRule rule, HttpStatus httpStatus) {
        super(rule.toString());
        this.httpStatus = httpStatus;
        this.clientMessage = rule.getMessage();
    }

    public FunctionalException(FunctionalRule rule, HttpStatus httpStatus, Throwable e) {
        super(rule.toString(), e);
        this.httpStatus = httpStatus;
        this.clientMessage = rule.getMessage();
    }

    public FunctionalException(FunctionalRule rule) {
        super(rule.toString());
        this.clientMessage = rule.getMessage();
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    public FunctionalException(FunctionalRule rule, Throwable e) {
        super(rule.toString(), e);
        this.clientMessage = rule.getMessage();
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

}
