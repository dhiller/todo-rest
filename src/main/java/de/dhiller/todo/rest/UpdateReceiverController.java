package de.dhiller.todo.rest;

import de.dhiller.todo.persistence.UpdateReceiver;
import de.dhiller.todo.persistence.UpdateReceiverRepository;
import de.dhiller.todo.persistence.User;
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

    @PutMapping("/updates")
    public void addUpdateReceiver(@RequestParam(value = "auth", required = false) String token,
                                  @RequestBody UpdateReceiverDTO updateReceiver) {
        User authorizedUser = authenticationProvider.authorize(token);
        UpdateReceiver newReceiver = modelMapper.map(updateReceiver, UpdateReceiver.class);
        newReceiver.setUser(authorizedUser);
        updateReceiverRepository.save(newReceiver);
    }

    @GetMapping("/updates")
    public List<UpdateReceiverDTO> getUpdateReceivers(@RequestParam(value = "auth", required = false) String token) {
        return updateReceiverRepository.findByUser(authenticationProvider.authorize(token)).stream()
                .map(r -> modelMapper.map(r, UpdateReceiverDTO.class))
                .collect(Collectors.toList());
    }

}
