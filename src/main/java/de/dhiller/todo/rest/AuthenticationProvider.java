package de.dhiller.todo.rest;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.Hashing;
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
        userRepository.findByUsername(username)
                .stream()
                .filter(u -> u.getPassword().equals(Hashing.sha256().hashString(password + u.getSalt(), Charsets.UTF_8).toString()))
                .findAny()
                .orElseThrow(UserNotFoundException::new);
        return userToAuthenticationToken.computeIfAbsent(username, (u) -> UUID.randomUUID().toString());
    }

    public User authorize(String token) {
        String username = ofNullable(userToAuthenticationToken.inverse().get(token)).orElseThrow(UserUnauthorizedException::new);
        return userRepository.findByUsername(username).stream().findAny().orElseThrow(UserNotFoundException::new);
    }

}
