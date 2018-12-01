package de.dhiller.todo.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void authenticationFails() throws Exception {
        this.mockMvc.perform(
                post("/authenticate")
                        .param("username", "jdee").param("password", "mys3cr3t"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void authenticationShouldReturnToken() throws Exception {
        assertThat(this.mockMvc.perform(
                post("/authenticate")
                        .param("username", "jdoe").param("password", "mys3cr3t"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/plain;charset=UTF-8")))
                .andReturn().getResponse().getContentAsString().matches("[0-9a-f-]+"), is(true));
    }

}
