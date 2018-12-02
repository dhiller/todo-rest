package de.dhiller.todo.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected String authenticate(String username, String password) throws Exception {
        return performAuthentication(username, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    protected ResultActions performAuthentication(String username, String password) throws Exception {
        return this.mockMvc.perform(
                post("/authenticate").param("username", username).param("password", password));
    }

    protected void performPutUpdateReceiver(UpdateReceiverDTO updateReceiver, String authenticationToken) throws Exception {
        this.mockMvc.perform(
                put("/updates")
                        .param("auth", authenticationToken)
                        .content(objectMapper.writeValueAsString(updateReceiver))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    protected void performUpdate(String authenticationToken, TodoDTO todoDTO) throws Exception {
        this.mockMvc.perform(
                post("/todos/1")
                        .param("auth", authenticationToken)
                        .content(objectMapper.writeValueAsString(todoDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    protected String authenticateJaneDoe() throws Exception {
        return authenticate("jdoe", "mys3cr3t");
    }

    protected String authenticateJaneDae() throws Exception {
        return authenticate("jdae", "my0th3rs3cr3t");
    }
}
