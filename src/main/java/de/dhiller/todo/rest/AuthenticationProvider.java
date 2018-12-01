package de.dhiller.todo.rest;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.dhiller.todo.persistence.User;
import de.dhiller.todo.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Optional.ofNullable;

@Component
public class AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    private final BiMap<String, String> userToAuthenticationToken = HashBiMap.create();

    public String authenticate(String username, String password) {
        userRepository.findByUsername(username).stream().findAny().orElseThrow(() -> new UserNotFoundException());
        return userToAuthenticationToken.computeIfAbsent(username, (u) -> UUID.randomUUID().toString());
    }

    public User authorize(String token) {
        String username = ofNullable(userToAuthenticationToken.inverse().get(token)).orElseThrow(() -> new UserUnauthorizedException());
        return userRepository.findByUsername(username).stream().findAny().orElseThrow(() -> new UserNotFoundException());
    }

}
