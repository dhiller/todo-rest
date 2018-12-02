package de.dhiller.todo.rest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dhiller.todo.persistence.*;
import io.swagger.annotations.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;

import static java.util.Optional.ofNullable;

@RestController
public class TodoController {

    private static final Pattern MATCH_ALL_PATTERN = Pattern.compile(".*");

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UpdateReceiverRepository updateReceiverRepository;

    private RestTemplate restTemplate
            = new RestTemplateBuilder()
            .setConnectTimeout(3000)
            .errorHandler(new ResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) throws IOException {
                    return !response.getStatusCode().is2xxSuccessful();
                }

                @Override
                public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
                    log.info("failed to notify client {}, response is {}", url, response);
                    handleError(response);
                }

                @Override
                public void handleError(ClientHttpResponse response) throws IOException {
                    if (response.getStatusCode().is4xxClientError()) {
                        log.error("failed to notify client, response is {}", response);
                        throw new HttpClientErrorException(response.getStatusCode());
                    } else if (response.getStatusCode().is5xxServerError()) {
                        log.warn("server error while trying to notify remote, response is {}", response);
                        throw new HttpServerErrorException(response.getStatusCode());
                    } else {
                        log.warn("server error while trying to notify remote, response is {}", response);
                    }
                }
            })
            .build();

    @ApiOperation(value="Returns a list of todo items for the authenticated user.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of todo items for the current user (possibly filtered).", response = TodoDTO.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource.")
    })
    @GetMapping("/todos")
    public List<TodoDTO> listTodos(
            @ApiParam(value="The access token")
            @RequestParam(value = "auth", required = false) String token,
            @ApiParam(value="The item filter comprising of a todo item, i.e. {\"id\":null,\"done\":null,\"content\":\".*kitchen.*\"}")
            @RequestBody(required = false) TodoDTO filter) {
        Pattern filterPattern = ofNullable(filter).filter(f -> f.getContent() != null)
                .map(f -> Pattern.compile(f.getContent()))
                .orElse(MATCH_ALL_PATTERN);
        log.debug("filtering responses by pattern {}", filterPattern);
        return todoRepository.findByUser(authenticationProvider.authorize(token)).stream()
                .filter(t -> filterPattern.matcher(t.getContent()).matches())
                .map(t -> modelMapper.map(t, TodoDTO.class))
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value="Updates a todo item for the authenticated user.",
            response = List.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the update has succeeded"),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource"),
            @ApiResponse(code = 404, message = "The todo item has not been found")
    })
    @PostMapping("/todos/{id}")
    public void updateTodo(
            @ApiParam(value = "The access token")
            @RequestParam(value = "auth", required = false) String token,
            @ApiParam(value = "The id of the item to update")
            @PathVariable(value = "id") long id,
            @ApiParam(value = "The updated item as json")
            @RequestBody TodoDTO update) {
        final User authorizedUser = authenticationProvider.authorize(token);
        Todo todoToUpdate = getTodoByIdOrThrowNotFound(id);
        if (!authorizedUser.equals(todoToUpdate.getUser()))
            throw new TodoNotFoundException();
        todoToUpdate.setContent(update.getContent());
        todoToUpdate.setDone(update.isDone());
        final TodoDTO afterUpdate = modelMapper.map(todoToUpdate, TodoDTO.class);
        todoRepository.save(todoToUpdate);
        notifyUpdateReceivers(authorizedUser, afterUpdate);
    }

    @ApiOperation(value="Creates a todo item for the authenticated user.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the creation has succeeded", response = TodoDTO.class),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource")
    })
    @PutMapping("/todos")
    public TodoDTO insertTodo(
            @ApiParam(value = "The access token")
            @RequestParam(value = "auth", required = false) String token,
            @ApiParam(value = "The new item as json")
            @RequestBody TodoDTO update) {
        final User authorizedUser = authenticationProvider.authorize(token);
        Todo todoToInsert = modelMapper.map(update, Todo.class);
        todoToInsert.setUser(authorizedUser);
        todoRepository.save(todoToInsert);
        final TodoDTO afterInsert = modelMapper.map(todoToInsert, TodoDTO.class);
        notifyUpdateReceivers(authorizedUser, afterInsert);
        return afterInsert;
    }

    @ApiOperation(value="Deletes a todo item for the authenticated user.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the deletion has succeeded"),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource"),
            @ApiResponse(code = 404, message = "The todo item has not been found")
    })
    @DeleteMapping("/todos/{id}")
    public void deleteTodo(
            @ApiParam(value = "The access token")
            @RequestParam(value = "auth", required = false) String token,
            @ApiParam(value = "The id of the item to delete")
            @PathVariable(value = "id") long id) {
        final User authorizedUser = authenticationProvider.authorize(token);
        Todo todoToUpdate = getTodoByIdOrThrowNotFound(id);
        if (!authorizedUser.equals(todoToUpdate.getUser()))
            throw new TodoNotFoundException();
        final TodoDTO afterUpdate = modelMapper.map(todoToUpdate, TodoDTO.class);
        todoRepository.delete(todoToUpdate);
        notifyUpdateReceivers(authorizedUser, afterUpdate);
    }

    private void notifyUpdateReceivers(User authorizedUser, TodoDTO afterUpdate) {
        updateReceiverRepository.findByUser(authorizedUser).stream()
                .map(e -> callEndpoint(e, afterUpdate))
                .forEach(executor::submit);
    }

    private Runnable callEndpoint(final UpdateReceiver updateReceiver, final TodoDTO afterUpdate) {
        return () -> {
            try {
                restTemplate.put(updateReceiver.getEndpoint(), afterUpdate);
            } catch (Exception ex) {
                log.error("error while trying to notify remote", ex);
                throw new RuntimeException(ex);
            }
        };
    }

    @ApiOperation(value="Returns a single todo item for the authenticated user.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The todo item for the current user", response = TodoDTO.class),
            @ApiResponse(code = 401, message = "The token is not authorized to access this resource"),
            @ApiResponse(code = 404, message = "The todo item has not been found")
    })
    @GetMapping("/todos/{id}")
    public TodoDTO getTodo(@RequestParam(value = "auth", required = false) String token,
                           @PathVariable(value = "id") long id) {
        User authorizedUser = authenticationProvider.authorize(token);
        Todo todoToUpdate = getTodoByIdOrThrowNotFound(id);
        if (!authorizedUser.equals(todoToUpdate.getUser()))
            throw new TodoNotFoundException();
        return modelMapper.map(todoToUpdate, TodoDTO.class);
    }

    private Todo getTodoByIdOrThrowNotFound(long id) {
        return todoRepository.findById(id).orElseThrow(TodoNotFoundException::new);
    }

}
