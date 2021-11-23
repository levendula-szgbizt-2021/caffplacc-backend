package hu.bme.szgbizt.levendula.caffplacc.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRefreshRequest implements Serializable {

    private String refreshToken;
}
