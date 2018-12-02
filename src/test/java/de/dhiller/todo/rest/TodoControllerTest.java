package de.dhiller.todo.rest;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import javax.transaction.Transactional;

public class TodoControllerTest extends ControllerTestBase {

    private String authenticationToken;
    private String otherAuthenticationToken;

    private final TodoDTO todo = new TodoDTO(true, "Clean up the kitchen (finished)");

    @Before
    public void requestAuthentication() throws Exception {
        authenticationToken = authenticateJaneDoe();
        otherAuthenticationToken = authenticateJaneDae();
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
    public void updateItemForWrongUserIsNotFound() throws Exception {
        this.mockMvc.perform(
                post("/todos/1")
                        .param("auth", otherAuthenticationToken)
                        .content(objectMapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItem() throws Exception {
        performUpdate(this.authenticationToken, todo);

        this.mockMvc.perform(
                get("/todos/1").param("auth", this.authenticationToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Clean up the kitchen (finished)"))
                .andExpect(jsonPath("$.done").value("true"));
    }

    @Test
    @Transactional
    public void insertItem() throws Exception {
        this.mockMvc.perform(get("/todos").param("auth", authenticationToken)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value("3"));

        String newItemAsString = this.mockMvc.perform(
                put("/todos")
                        .param("auth", this.authenticationToken)
                        .content(objectMapper.writeValueAsString(new TodoDTO(false, "Mow the lawn")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TodoDTO newItem = objectMapper.readValue(newItemAsString, TodoDTO.class);

        this.mockMvc.perform(
                get("/todos/" + newItem.getId()).param("auth", authenticationToken))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/todos").param("auth", authenticationToken)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value("4"));
    }

    @Test
    @Transactional
    public void deleteItem() throws Exception {
        this.mockMvc.perform(
                delete("/todos/1")
                        .param("auth", this.authenticationToken))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(
                get("/todos/1").param("auth", authenticationToken))
                .andExpect(status().isNotFound());
    }

}
