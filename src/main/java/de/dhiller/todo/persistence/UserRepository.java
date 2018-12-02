package de.dhiller.todo.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findByUsernameAndPassword(String username, String password);

    List<User> findByUsername(String username);

}
