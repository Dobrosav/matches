package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username="test@example.com")
    void whenGetUserByMail_thenReturnUserResponse() throws Exception {
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail("test@example.com");
        userResponse.setName("Test");

        given(userService.getUserByMail("test@example.com")).willReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/test@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenDeleteUser_thenReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/users/test@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenUpdatePreferences_thenReturnUpdatedUser() throws Exception {
        UserPreferencesRequest preferencesRequest = new UserPreferencesRequest();
        preferencesRequest.setTargetGender("FEMALE");
        preferencesRequest.setMinAge(25);
        preferencesRequest.setMaxAge(35);

        UserResponse updatedUserResponse = new UserResponse();
        updatedUserResponse.setEmail("test@example.com");


        given(userService.updatePreferences(eq("test@example.com"), any(UserPreferencesRequest.class)))
                .willReturn(updatedUserResponse);

        mockMvc.perform(put("/api/v1/users/test@example.com/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferencesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenUploadImage_thenReturnImageResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        UserImageResponse imageResponse = new UserImageResponse(1, "hello.txt", "image/jpeg", true);
        given(userService.uploadImage(eq("test@example.com"), any())).willReturn(imageResponse);

        mockMvc.perform(multipart("/api/v1/users/test@example.com/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("hello.txt"));
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenGetUserImages_thenReturnListOfImages() throws Exception {
        UserImageResponse imageResponse = new UserImageResponse(1, "test.jpg", "image/jpeg", true);
        given(userService.getUserImages("test@example.com")).willReturn(List.of(imageResponse));

        mockMvc.perform(get("/api/v1/users/test@example.com/images")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("test.jpg"));
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenDeleteImage_thenReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/users/test@example.com/images/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        verify(userService).deleteImage("test@example.com", 1);
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenSetProfileImage_thenReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/v1/users/test@example.com/images/1/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        verify(userService).setProfileImage("test@example.com", 1);
    }
}
