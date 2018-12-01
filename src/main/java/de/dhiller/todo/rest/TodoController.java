package de.dhiller.todo.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dhiller.todo.persistence.TodoRepository;
import de.dhiller.todo.persistence.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    @RequestMapping("/todos")
    public List<TodoDTO> listTodos(@RequestParam(value = "auth", defaultValue = "") String token) {
        return todoRepository.findByUser(authenticationProvider.authorize(token)).stream()
                .map(t -> modelMapper.map(t, TodoDTO.class))
                .collect(Collectors.toList());
    }
}
