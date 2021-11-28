package hu.bme.szgbizt.levendula.caffplacc.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

@Getter
public class CaffplaccError implements Serializable {
    private final Instant timestamp;

    private final int status;

    private final String error;

    private final String path;

    @JsonCreator
    public CaffplaccError(@JsonProperty("timestamp") Instant timestamp, @JsonProperty("status") int status, @JsonProperty("error") String error, @JsonProperty("path") String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.path = path;
    }

    @Override
    public String toString() {
        return "BusinessValidationError{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

}