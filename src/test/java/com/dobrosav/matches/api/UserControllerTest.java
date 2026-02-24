package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.*;
import com.dobrosav.matches.api.model.response.*;
import com.dobrosav.matches.db.entities.Sex;
import com.dobrosav.matches.db.entities.UserImage;
import com.dobrosav.matches.security.AuthenticationResponse;
import com.dobrosav.matches.security.AuthenticationService;
import com.dobrosav.matches.service.ChatService;
import com.dobrosav.matches.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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

    @MockBean
    private ChatService chatService;

    @MockBean
    private AuthenticationService authenticationService;

    // --- AUTHENTICATION ENDPOINTS ---

    @Test
    void whenRegister_thenReturnAuthenticationResponse() throws Exception {
        UserRequest request = new UserRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password");
        request.setName("John");
        request.setSurname("Doe");
        request.setUsername("johndoe");
        request.setSex(Sex.MALE);
        request.setDateOfBirth(new java.util.Date());

        AuthenticationResponse response = new AuthenticationResponse("access-token", "refresh-token");

        given(authenticationService.register(any(UserRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
    }

    @Test
    void whenLogin_thenReturnAuthenticationResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthenticationResponse response = new AuthenticationResponse("access-token", "refresh-token");

        given(authenticationService.authenticate(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
    }

    @Test
    void whenRefreshToken_thenReturnAuthenticationResponse() throws Exception {
        AuthenticationResponse response = new AuthenticationResponse("new-access-token", "new-refresh-token");

        given(authenticationService.refreshToken(any(HttpServletRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.refresh_token").value("new-refresh-token"));
    }

    // --- USER PROFILE ENDPOINTS ---

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
        
        verify(userService).deleteUser("test@example.com");
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenGetUsersByAge_thenReturnListOfUsers() throws Exception {
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail("young@example.com");
        
        given(userService.findByAge(20, 30)).willReturn(List.of(userResponse));

        mockMvc.perform(get("/api/v1/users")
                        .param("beginYear", "20")
                        .param("endYear", "30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("young@example.com"));
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
    void whenSetPremium_thenReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/v1/users/test@example.com/premium")
                        .param("isPremium", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
                
        verify(userService).setPremium("test@example.com", true);
    }

    // --- IMAGES ENDPOINTS ---

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
    void whenGetImage_thenReturnImageBytes() throws Exception {
        UserImage userImage = new UserImage();
        userImage.setFileName("test.jpg");
        userImage.setContentType("image/jpeg");
        userImage.setContent("fake-image-data".getBytes());
        
        given(userService.getImage("test@example.com", 1)).willReturn(userImage);
        
        mockMvc.perform(get("/api/v1/users/test@example.com/images/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.jpg\""))
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes("fake-image-data".getBytes()));
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

    // --- INTERACTIONS ENDPOINTS (LIKE, DISLIKE, MATCHES, FEED) ---
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenLikeUser_thenReturnSuccessResultWithMatchStatus() throws Exception {
        given(userService.likeUser("test@example.com", 2)).willReturn(true);
        
        mockMvc.perform(post("/api/v1/users/test@example.com/like/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser(username="test@example.com")
    void whenDislikeUser_thenReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/users/test@example.com/dislike/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
                
        verify(userService).dislikeUser("test@example.com", 2);
    }
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenGetMatches_thenReturnListOfMatches() throws Exception {
        MatchResponse matchResponse = new MatchResponse();
        matchResponse.setMatchId(10);
        
        given(userService.getMatches("test@example.com")).willReturn(List.of(matchResponse));
        
        mockMvc.perform(get("/api/v1/users/test@example.com/matches")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchId").value(10));
    }
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenGetFeed_thenReturnListOfUsers() throws Exception {
        UserResponse feedUser = new UserResponse();
        feedUser.setEmail("feed@example.com");
        
        given(userService.getFeed("test@example.com")).willReturn(List.of(feedUser));
        
        mockMvc.perform(get("/api/v1/users/test@example.com/feed")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("feed@example.com"));
    }
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenGetFilteredFeed_thenReturnListOfUsers() throws Exception {
        UserResponse feedUser = new UserResponse();
        feedUser.setEmail("filtered@example.com");
        
        given(userService.getFilteredFeed("test@example.com", "MALE", 20, 30, "Belgrade"))
                .willReturn(List.of(feedUser));
        
        mockMvc.perform(get("/api/v1/users/test@example.com/filtered-feed")
                        .param("gender", "MALE")
                        .param("minAge", "20")
                        .param("maxAge", "30")
                        .param("location", "Belgrade")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("filtered@example.com"));
    }
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenSearchUsers_thenReturnListOfUsers() throws Exception {
        UserResponse searchUser = new UserResponse();
        searchUser.setEmail("search@example.com");
        
        given(userService.searchUsers("test@example.com")).willReturn(List.of(searchUser));
        
        mockMvc.perform(get("/api/v1/users/test@example.com/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("search@example.com"));
    }
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenGetLikers_thenReturnListOfUsers() throws Exception {
        UserResponse liker = new UserResponse();
        liker.setEmail("liker@example.com");
        
        given(userService.getLikers("test@example.com")).willReturn(List.of(liker));
        
        mockMvc.perform(get("/api/v1/users/test@example.com/likers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("liker@example.com"));
    }

    // --- CHAT ENDPOINTS ---
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenSendChatMessage_thenReturnSavedMessage() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderId(1);
        request.setContent("Hello there");
        
        ChatMessageResponse response = new ChatMessageResponse(1, 1, 2, "Sender", "Hello there", new java.util.Date());
        
        given(chatService.saveMessage(10, 1, "Hello there")).willReturn(response);
        
        mockMvc.perform(post("/api/v1/matches/10/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello there"));
    }
    
    @Test
    @WithMockUser(username="test@example.com")
    void whenGetChatHistory_thenReturnListOfMessages() throws Exception {
        ChatMessageResponse msg = new ChatMessageResponse(1, 1, 2, "Sender", "Hi", new java.util.Date());
        
        given(chatService.getChatHistory(10)).willReturn(List.of(msg));
        
        mockMvc.perform(get("/api/v1/matches/10/messages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hi"));
    }

}
