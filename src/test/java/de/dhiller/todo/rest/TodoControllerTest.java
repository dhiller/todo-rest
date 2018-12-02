package de.dhiller.todo.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String authenticationToken;

    @Before
    public void requestAuthentication() throws Exception {
        authenticationToken = this.mockMvc.perform(
                post("/authenticate")
                        .param("username", "jdoe").param("password", "mys3cr3t"))
                .andReturn().getResponse().getContentAsString();
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

}
