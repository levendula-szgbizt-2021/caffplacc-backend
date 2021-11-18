package hu.bme.szgbizt.levendula.caffplacc.configuration;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccError;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@Primary
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CaffplaccExceptionHandler {

    @ExceptionHandler(CaffplaccException.class)
    public final ResponseEntity<CaffplaccError> handleSurveyException(CaffplaccException e, WebRequest request) {
        CaffplaccError error = new CaffplaccError(
                Instant.now(),
                e.getStatusCode(),
                e.getStatusMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, e.getStatus());
    }
}