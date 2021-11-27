package hu.bme.szgbizt.levendula.caffplacc.user;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.login.UserDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserProfileIF {

    @ApiOperation(value = "changeUsernameOrPassword", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an user to update their username or password.")
    @PostMapping("changeUsernameOrPassword")
    ResponseEntity<?> changeUsernameOrPassword(@RequestBody UserDto dto) throws CaffplaccException;
}
