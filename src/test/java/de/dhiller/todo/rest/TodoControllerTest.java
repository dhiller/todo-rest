package de.dhiller.todo.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String authenticationToken;
    private String otherAuthenticationToken;

    @Before
    public void requestAuthentication() throws Exception {
        authenticationToken = authenticate("jdoe", "mys3cr3t");
        otherAuthenticationToken = authenticate("jdae", "my0th3rs3cr3t");
    }

    @Test
    public void noTokenShouldBeUnauthorized() throws Exception {
        this.mockMvc.perform(get("/todos")).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNonEmptyList() throws Exception {
        this.mockMvc.perform(get("/todos").param("auth", authenticationToken)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].content").value("Clean up the kitchen"));
    }

    @Test
    public void shouldReturnFilteredList() throws Exception {
        final TodoDTO todo = new TodoDTO();
        todo.setContent(".*kitchen.*");
        this.mockMvc.perform(
                get("/todos")
                        .param("auth", authenticationToken)
                        .content(objectMapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].content").value("Clean up the kitchen"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getTodoById() throws Exception {
        this.mockMvc.perform(
                get("/todos/1").param("auth", authenticationToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Clean up the kitchen"))
                .andExpect(jsonPath("$.done").value("false"));
    }

    @Test
    public void getTodoByIdForWrongUser() throws Exception {
        this.mockMvc.perform(
                get("/todos/1").param("auth", otherAuthenticationToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItemWithoutTokenIsUnauthorized() throws Exception {
        final TodoDTO todo = new TodoDTO(true, "Clean up the kitchen (finished)");
        this.mockMvc.perform(
                post("/todos/1")
                        .content(objectMapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void updateItemForWrongUser() throws Exception {
        final TodoDTO todo = new TodoDTO(true, "Clean up the kitchen (finished)");
        this.mockMvc.perform(
                post("/todos/1")
                        .param("auth", otherAuthenticationToken)
                        .content(objectMapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItem() throws Exception {
        final TodoDTO todo = new TodoDTO(true, "Clean up the kitchen (finished)");
        this.mockMvc.perform(
                post("/todos/1")
                        .param("auth", authenticationToken)
                        .content(objectMapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        this.mockMvc.perform(
                get("/todos/1").param("auth", authenticationToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Clean up the kitchen (finished)"))
                .andExpect(jsonPath("$.done").value("true"));
    }

    private String authenticate(String username, String password) throws Exception {
        return this.mockMvc.perform(
                post("/authenticate")
                        .param("username", username).param("password", password))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

}
