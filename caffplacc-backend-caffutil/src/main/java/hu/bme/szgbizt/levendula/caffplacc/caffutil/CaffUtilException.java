package hu.bme.szgbizt.levendula.caffplacc.caffutil;

public class CaffUtilException extends RuntimeException {

    public CaffUtilException(String message) {
        super(message);
    }

    public CaffUtilException(String message, Throwable cause) {
        super(message, cause);
    }
}
