package de.dhiller.todo.rest;

import static org.mockserver.model.HttpRequest.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import org.springframework.http.MediaType;

import javax.transaction.Transactional;
import java.net.URI;

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

}
