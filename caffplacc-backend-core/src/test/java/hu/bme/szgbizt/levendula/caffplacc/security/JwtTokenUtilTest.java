package hu.bme.szgbizt.levendula.caffplacc.security;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.Key;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

    @Test
    void getUsernameFromToken() {
        String token = Jwts.builder().setSubject("Joe").signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
        assertEquals("Joe", jwtTokenUtil.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromTokenThrowsErrorForTokenNotSignedByKnownKey() {
        String key = "key";
        String token = Jwts.builder().setSubject("Joe").signWith(SignatureAlgorithm.HS512, key.getBytes()  ).compact();
        assertThrows(SignatureException.class, () -> jwtTokenUtil.getUsernameFromToken(token));
    }

    @Test
    void usingAnInvalidJWTShouldThrowException() {
        String wannabeToken = "djaesdivnaopsidnpaosinb";
        assertThrows(MalformedJwtException.class, () -> jwtTokenUtil.getUsernameFromToken(wannabeToken));
    }

    @Test
    void getExpirationDateFromToken() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        Date date = c.getTime();
        c.add(Calendar.YEAR, 2);
        Date expDate = c.getTime();
        String token = Jwts.builder().setSubject("Joe").setIssuedAt(date).setExpiration(expDate).signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
        assertEquals(expDate.toString(), jwtTokenUtil.getExpirationDateFromToken(token).toString());
    }

    @Test
    void getExpirationDateFromTokenThrowsErrorIfTokenHasExpired() {
        Date date = new Date(System.currentTimeMillis());
        String token = Jwts.builder().setSubject("Joe").setIssuedAt(date).setExpiration(date).signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
        assertThrows(ExpiredJwtException.class, () -> jwtTokenUtil.getExpirationDateFromToken(token));
    }

    @Test
    void generateToken() {
        UserDetails userDetails = new User("Test", "pass", List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.toString())));
        String token = jwtTokenUtil.generateToken(userDetails);
        assertEquals("Test", Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody().getSubject());
        assertEquals("[{authority=ROLE_USER}]", Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody().get("roles").toString());
        assertEquals(SecurityConstants.EXPIRATION_TIME * 1000,Jwts.parser().setSigningKey(SecurityConstants.SECRET).
                parseClaimsJws(token).getBody().getExpiration().getTime()
                -
                Jwts.parser().setSigningKey(SecurityConstants.SECRET).
                        parseClaimsJws(token).getBody().getIssuedAt().getTime());
    }

    @Test
    void generateRefreshToken() {
        UserDetails userDetails = new User("Test", "pass", List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.toString())));
        String token = jwtTokenUtil.generateRefreshToken(userDetails);
        assertEquals("Test", Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody().getSubject());
        assertEquals(SecurityConstants.REFRESH_EXPIRATION_TIME * 1000,Jwts.parser().setSigningKey(SecurityConstants.SECRET).
                parseClaimsJws(token).getBody().getExpiration().getTime()
                -
                Jwts.parser().setSigningKey(SecurityConstants.SECRET).
                        parseClaimsJws(token).getBody().getIssuedAt().getTime());
    }

    @Test
    void validateTokenForOtherUserShouldNotBeValidForAnOtherUser() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        Date date = c.getTime();
        c.add(Calendar.YEAR, 2);
        Date expDate = c.getTime();
        UserDetails userDetails = new User("Test", "pass", List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.toString())));
        UserDetails userDetails2 = new User("Test2", "pass", List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.toString())));
        String token = jwtTokenUtil.generateToken(userDetails);
        String token2 = jwtTokenUtil.generateToken(userDetails2);
        assertFalse(jwtTokenUtil.validateToken(token, userDetails2));
    }
}