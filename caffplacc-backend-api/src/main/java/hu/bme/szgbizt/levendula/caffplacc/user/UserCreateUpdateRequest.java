package hu.bme.szgbizt.levendula.caffplacc.user;

import hu.bme.szgbizt.levendula.caffplacc.Password;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateUpdateRequest {

    String username;
    @Password
    @Size(min = 8, message = "Password size must be at least 8 characters.")
    String password;
    String email;
    boolean admin;
}
