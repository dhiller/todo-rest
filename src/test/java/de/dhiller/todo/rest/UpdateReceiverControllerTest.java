package de.dhiller.todo.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UpdateReceiverControllerTest {

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
    @Transactional
    public void putUpdate() throws Exception {
        final UpdateReceiverDTO updateReceiver = new UpdateReceiverDTO();
        updateReceiver.setEndpoint(URI.create("http://localhost:8080/"));
        this.mockMvc.perform(
                put("/updates")
                        .param("auth", authenticationToken)
                        .content(objectMapper.writeValueAsString(updateReceiver))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(
                get("/updates").param("auth", authenticationToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].endpoint").value("http://localhost:8080/"));
    }

    private String authenticate(String username, String password) throws Exception {
        return this.mockMvc.perform(
                post("/authenticate")
                        .param("username", username).param("password", password))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

}
