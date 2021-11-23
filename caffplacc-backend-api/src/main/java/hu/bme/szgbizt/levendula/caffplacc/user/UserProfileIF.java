package hu.bme.szgbizt.levendula.caffplacc.user;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserProfileIF {

    @PostMapping("changeUsernameOrPassword")
    ResponseEntity<?> changeUsernameOrPassword(@RequestBody UserDto dto) throws CaffplaccException;
}
