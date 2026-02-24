package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.ProfileUpdateRequest;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProfileControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redis.getMappedPort(6379)));
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1);
        currentUser.setEmail("test@example.com");
        currentUser.setName("Test");
        currentUser.setSurname("User");
        currentUser.setUsername("testuser");
        currentUser.setAdmin(false); // Fix NPE in getAuthorities()
        currentUser.setPremium(false);
    }

    @Test
    void testGetMyProfile() throws Exception {
        when(userService.findById(1)).thenReturn(currentUser);

        mockMvc.perform(get("/api/v1/profile/me")
                .with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.surname").value("User"));
    }

    @Test
    void testUpdateMyProfile() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setBio("New Bio");
        request.setLocation("New City");
        request.setInterests("New Interests");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("test@example.com");
        updatedUser.setBio("New Bio");
        updatedUser.setLocation("New City");
        
        when(userService.updateUserProfile(eq(1), any(ProfileUpdateRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/profile/me")
                .with(user(currentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("New Bio"))
                .andExpect(jsonPath("$.location").value("New City"));
    }
}