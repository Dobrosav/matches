package com.dobrosav.matches.api;
import com.dobrosav.matches.AbstractIntegrationTest;

import com.dobrosav.matches.api.model.request.ReactionRequest;
import com.dobrosav.matches.service.ReactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dobrosav.matches.security.JwtService;
import com.dobrosav.matches.security.JwtAuthenticationFilter;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReactionControllerTest extends AbstractIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReactionService reactionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testReact() throws Exception {
        ReactionRequest request = new ReactionRequest();
        request.setFromUserEmail("user1@example.com");
        request.setToUserEmail("user2@example.com");
        request.setReaction("like");

        doNothing().when(reactionService).processReaction("user1@example.com", "user2@example.com", "like");

        mockMvc.perform(post("/api/v1/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reactionService).processReaction("user1@example.com", "user2@example.com", "like");
    }
}
