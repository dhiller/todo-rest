package de.dhiller.todo.rest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationControllerTest extends ControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void authenticationFails() throws Exception {
        performAuthentication("jdee", "mys3cr3t")
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void authenticationShouldReturnToken() throws Exception {
        assertThat(performAuthentication("jdoe", "mys3cr3t")
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andReturn().getResponse().getContentAsString().matches("[0-9a-f-]+"), is(true));
    }

}
