package hu.bme.szgbizt.levendula.caffplacc.login;

import hu.bme.szgbizt.levendula.caffplacc.validation.Password;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    String username;
    @Password
    @NotEmpty
    String password;
    String email;
}