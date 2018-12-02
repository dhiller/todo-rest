package de.dhiller.todo.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dhiller.todo.persistence.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import static java.util.Optional.ofNullable;

@RestController
public class TodoController {

    private static final Pattern MATCH_ALL_PATTERN = Pattern.compile(".*");

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UpdateReceiverRepository updateReceiverRepository;

    private RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(10000).build();

    @GetMapping("/todos")
    public List<TodoDTO> listTodos(@RequestParam(value = "auth", required = false) String token,
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

    @PostMapping("/todos/{id}")
    public void updateTodo(@RequestParam(value = "auth", required = false) String token,
                           @PathVariable(value="id") long id,
                           @RequestBody(required = false) TodoDTO update) {
        final User authorizedUser = authenticationProvider.authorize(token);
        Todo todoToUpdate = getTodoByIdOrThrowNotFound(id);
        if(!authorizedUser.equals(todoToUpdate.getUser()))
            throw new TodoNotFoundException();
        todoToUpdate.setContent(update.getContent());
        todoToUpdate.setDone(update.isDone());
        final TodoDTO afterUpdate = modelMapper.map(todoToUpdate, TodoDTO.class);
        todoRepository.save(todoToUpdate);
        new Thread(() -> {
            updateReceiverRepository.findByUser(authorizedUser).forEach(
                   updateReceiver -> restTemplate.put(updateReceiver.getEndpoint(), afterUpdate)
            );
        }).run();
    }

    @GetMapping("/todos/{id}")
    public TodoDTO getTodo(@RequestParam(value = "auth", required = false) String token,
                           @PathVariable(value="id") long id) {
        User authorizedUser = authenticationProvider.authorize(token);
        Todo todoToUpdate = getTodoByIdOrThrowNotFound(id);
        if(!authorizedUser.equals(todoToUpdate.getUser()))
            throw new TodoNotFoundException();
        return modelMapper.map(todoToUpdate, TodoDTO.class);
    }

    private Todo getTodoByIdOrThrowNotFound(long id) {
        return todoRepository.findById(id).orElseThrow(TodoNotFoundException::new);
    }

}
