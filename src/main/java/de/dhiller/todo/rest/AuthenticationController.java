package de.dhiller.todo.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @ApiOperation(value="Authenticates a user using username and password and returns an access token if successful.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the authentication succeeded", response = UUID.class, responseContainer = "String"),
            @ApiResponse(code = 403, message = "If the authentication failed")
    })
    @PostMapping("/authenticate")
    public String authenticate(
            @ApiParam(value = "The username")
            @RequestParam("username") String username,
            @ApiParam(value = "The password")
            @RequestParam("password") String password) {
        return authenticationProvider.authenticate(username, password);
    }

}
