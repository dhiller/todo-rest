package de.dhiller.todo.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UpdateReceiverRepository extends CrudRepository<UpdateReceiver, Long> {

    List<UpdateReceiver> findByUser(User user);

}
