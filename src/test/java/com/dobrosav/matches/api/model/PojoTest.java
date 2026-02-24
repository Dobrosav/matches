package com.dobrosav.matches.api.model;

import com.dobrosav.matches.api.model.request.LoginRequest;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.api.model.response.LoginWrapper;
import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.response.SuccessResult;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.Sex;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PojoTest {

    @Test
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("password");
        assertEquals("test@email.com", request.getEmail());
        assertEquals("password", request.getPassword());
    }

    @Test
    void testUserRequest() {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setSurname("Doe");
        request.setSex(Sex.MALE);
        request.setUsername("johndoe");
        request.setDateOfBirth(new Date(1000000000000L));
        
        assertEquals("John", request.getName());
        assertEquals("Doe", request.getSurname());
        assertEquals(Sex.MALE, request.getSex());
        assertEquals("johndoe", request.getUsername());
        assertEquals(new Date(1000000000000L), request.getDateOfBirth());
    }

    @Test
    void testChatMessageResponse() {
        Date now = new Date();
        ChatMessageResponse response = new ChatMessageResponse(1, 10, 100, "Sender", "Content", now);
        assertEquals(1, response.getId());
        assertEquals(10, response.getMatchId());
        assertEquals(100, response.getSenderId());
        assertEquals("Sender", response.getSenderUsername());
        assertEquals("Content", response.getContent());
        assertEquals(now, response.getTimestamp());
        
        ChatMessageResponse empty = new ChatMessageResponse(2, 20, 3, "New Sender", "New Content", now);
        assertEquals(2, empty.getId());
        assertEquals(20, empty.getMatchId());
        assertEquals(3, empty.getSenderId());
        assertEquals("New Sender", empty.getSenderUsername());
        assertEquals("New Content", empty.getContent());
        assertEquals(now, empty.getTimestamp());
    }

    @Test
    void testLoginWrapper() {
        LoginWrapper wrapper = new LoginWrapper();
        UserResponse user = new UserResponse();
        SuccessResult res = new SuccessResult();
        wrapper.setUser(user);
        wrapper.setResult(res);
        assertEquals(user, wrapper.getUser());
        assertEquals(res, wrapper.getResult());
    }

    @Test
    void testMatchResponse() {
        MatchResponse response = new MatchResponse();
        response.setMatchId(1);
        UserResponse user = new UserResponse();
        response.setOtherUser(user);
        
        assertEquals(1, response.getMatchId());
        assertEquals(user, response.getOtherUser());
    }

    @Test
    void testSuccessResult() {
        SuccessResult result = new SuccessResult();
        result.setResult(true);
        assertEquals(true, result.getResult());
        
        SuccessResult argResult = new SuccessResult();
        argResult.setResult(false);
        assertEquals(false, argResult.getResult());
    }
}
