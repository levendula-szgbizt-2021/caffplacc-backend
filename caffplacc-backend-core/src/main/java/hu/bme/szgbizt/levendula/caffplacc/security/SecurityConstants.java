package hu.bme.szgbizt.levendula.caffplacc.security;

public class SecurityConstants {
    public static final String SECRET = "SECRET_KEY";
    public static final long EXPIRATION_TIME = 30; // in seconds
    public static final long REFRESH_EXPIRATION_TIME = 60; // in seconds
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}