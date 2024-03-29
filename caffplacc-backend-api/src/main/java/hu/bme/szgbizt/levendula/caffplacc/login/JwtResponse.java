package hu.bme.szgbizt.levendula.caffplacc.login;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtResponse {

    static final long serialVersionUID = -8091879091924046844L;

    final String token;
    final String refreshToken;
    final String userId;
    final String username;
    final List<String> roles;
}