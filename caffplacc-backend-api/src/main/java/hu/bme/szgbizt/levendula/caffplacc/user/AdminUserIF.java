package hu.bme.szgbizt.levendula.caffplacc.user;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

public interface AdminUserIF {

    @ApiOperation(value = "listUsers", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an administrator privileges to list the registered users.")
    @GetMapping
    Page<AdminUserResponse> listUsers(@RequestParam(required = false, name = "username") String username, Pageable pageable);

    @ApiOperation(value = "getOneUser", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an administrator to get one user by their userId.")
    @GetMapping("/{id}")
    AdminUserResponse getOneUser(@PathVariable String id);

    @ApiOperation(value = "createUser", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an administrator to create a new user or administrator.")
    @PostMapping
    AdminUserResponse createUser(@RequestBody @Valid UserCreateUpdateRequest request);

    @ApiOperation(value = "updateUser", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by an administrator to update the information of any one user.")
    @PutMapping("/{id}")
    AdminUserResponse updateUser(@PathVariable String id, @RequestBody @Valid UserCreateUpdateRequest request);

    @ApiOperation(value = "deleteUser", notes = "This endpoint can be called by an administrator to delete any one user.")
    @DeleteMapping("/{id}")
    void deleteUser(@PathVariable String id);
}
