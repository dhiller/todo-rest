package de.dhiller.todo.rest;

import de.dhiller.todo.persistence.UpdateReceiver;
import de.dhiller.todo.persistence.UpdateReceiverRepository;
import de.dhiller.todo.persistence.User;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UpdateReceiverController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private UpdateReceiverRepository updateReceiverRepository;

    @ApiOperation(value="Registers a watch, meaning a URI that is called with the updated todo item for the authenticated user.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the creation has succeeded", response = TodoDTO.class),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource.")
    })
    @PutMapping("/updates")
    public void addUpdateReceiver(
            @ApiParam(value = "The access token")
            @RequestParam(value = "auth", required = false) String token,
            @ApiParam(value = "The callback url to be notified for changes to todo items")
            @RequestBody UpdateReceiverDTO updateReceiver) {
        User authorizedUser = authenticationProvider.authorize(token);
        UpdateReceiver newReceiver = modelMapper.map(updateReceiver, UpdateReceiver.class);
        newReceiver.setUser(authorizedUser);
        updateReceiverRepository.save(newReceiver);
    }

    @ApiOperation(value="Returns the list of watches, meaning URIs that are called with the updated todo item for the authenticated user.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of watches that have been registered", response = TodoDTO.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource.")
    })
    @GetMapping("/updates")
    public List<UpdateReceiverDTO> getUpdateReceivers(
            @ApiParam(value = "The access token")
            @RequestParam(value = "auth", required = false) String token) {
        return updateReceiverRepository.findByUser(authenticationProvider.authorize(token)).stream()
                .map(r -> modelMapper.map(r, UpdateReceiverDTO.class))
                .collect(Collectors.toList());
    }

}
