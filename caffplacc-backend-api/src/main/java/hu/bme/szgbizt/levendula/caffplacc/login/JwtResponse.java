package hu.bme.szgbizt.levendula.caffplacc.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String token;
}