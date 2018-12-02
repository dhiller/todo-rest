package de.dhiller.todo.rest;

import org.junit.Before;
import org.junit.Test;

import javax.transaction.Transactional;
import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateReceiverControllerTest extends ControllerTestBase {

    private String authenticationToken;
    private String otherAuthenticationToken;

    @Before
    public void requestAuthentication() throws Exception {
        authenticationToken = authenticateJaneDoe();
        otherAuthenticationToken = authenticateJaneDae();
    }

    @Test
    @Transactional
    public void putUpdate() throws Exception {
        performPutUpdateReceiver(new UpdateReceiverDTO(URI.create("http://localhost:8080/")), authenticationToken);

        this.mockMvc.perform(
                get("/updates").param("auth", authenticationToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].endpoint").value("http://localhost:8080/"));

        this.mockMvc.perform(
                get("/updates").param("auth", otherAuthenticationToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value("0"));
    }

}
