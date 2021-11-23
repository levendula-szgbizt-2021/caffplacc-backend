package hu.bme.szgbizt.levendula.caffplacc.user;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

public interface AdminUserIF {
    @ApiOperation(value = "listUsers", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @GetMapping
    Page<UserResponse> listUsers(@RequestParam("query") String query, Pageable pageable);

    @ApiOperation(value = "getOneUser", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @GetMapping("/{id}")
    UserResponse getOneUser(@PathVariable String id);

    @ApiOperation(value = "createUser", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @PostMapping
    UserResponse createUser(@RequestBody UserCreateRequest request);

    @ApiOperation(value = "updateUser", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @PutMapping("/{id}")
    UserResponse updateUser(@PathVariable String id, @RequestBody UserUpdateRequest request);

    @ApiOperation(value = "deleteUser", notes = "")
    @DeleteMapping("/{id}")
    void deleteUser(@PathVariable String id);

}
