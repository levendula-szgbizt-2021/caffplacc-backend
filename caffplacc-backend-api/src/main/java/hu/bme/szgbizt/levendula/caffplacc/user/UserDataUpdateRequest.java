package hu.bme.szgbizt.levendula.caffplacc.user;

import hu.bme.szgbizt.levendula.caffplacc.validation.Password;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDataUpdateRequest {
    String username;
    @Password
    String password;
    String email;
}
