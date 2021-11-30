package hu.bme.szgbizt.levendula.caffplacc.login;

import hu.bme.szgbizt.levendula.caffplacc.Password;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtRequest implements Serializable {

    static final long serialVersionUID = 5926468583005150707L;

    String username;
    @Password
    @Size(min = 8, message = "Password size must be at least 8 characters.")
    String password;
}