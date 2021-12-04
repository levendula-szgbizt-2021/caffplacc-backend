package hu.bme.szgbizt.levendula.caffplacc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CaffplaccParseException extends RuntimeException {
    private final HttpStatus status;

    private final int statusCode;

    private final String statusMessage;

    private final String field;

    private final String rejectedValue;


    @Getter
    private final String errorCode = this.getClass().getCanonicalName();

    public CaffplaccParseException(String message) {
        this(message, null, null);
    }

    public CaffplaccParseException(String message, String field) {
        this(message, field, null);
    }

    public CaffplaccParseException(String message, String field, Object rejectedValue) {
        super(message);

        if (rejectedValue != null) {
            this.rejectedValue = rejectedValue.toString();
        } else {
            this.rejectedValue = null;
        }
        this.field = field;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.statusMessage = message;
    }
}
