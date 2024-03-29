package hu.bme.szgbizt.levendula.caffplacc.user;

import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface UserProfileIF {

    @ApiOperation(value = "getUserData", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an user to get their user data.")
    @GetMapping
    UserResponse getUserData() throws CaffplaccException;

    @ApiOperation(value = "changeUserData", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an user to update their user data.")
    @PostMapping
    UserResponse changeUserData(@RequestBody @Valid UserDataUpdateRequest request);

    @ApiOperation(value = "deleteUserData", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an user to delete their account.")
    @DeleteMapping
    void deleteUserData() throws CaffplaccException;
}
