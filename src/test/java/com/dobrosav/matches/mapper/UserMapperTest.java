package com.dobrosav.matches.mapper;

import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.Sex;
import com.dobrosav.matches.db.entities.User;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void testToDto_NullUser() {
        assertNull(userMapper.toDto(null));
    }

    @Test
    void testToDto_ValidUser() {
        User user = new User();
        user.setId(1);
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john@example.com");
        user.setPremium(true);
        user.setAdmin(false);
        user.setSex(Sex.MALE);
        user.setUsername("johndoe");
        user.setDateOfBirth(new Date(1000000000000L));
        user.setDisability("None");
        user.setLocation("New York");
        user.setBio("Hello world");
        user.setInterests("Coding, Reading");

        UserResponse response = userMapper.toDto(user);

        assertEquals(1, response.getId());
        assertEquals("John", response.getName());
        assertEquals("Doe", response.getSurname());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(true, response.getPremium());
        assertEquals(false, response.getAdmin());
        assertEquals(Sex.MALE, response.getSex());
        assertEquals("johndoe", response.getUsername());
        assertEquals(new Date(1000000000000L), response.getDateOfBirth());
        assertEquals("None", response.getDisability());
        assertEquals("New York", response.getLocation());
        assertEquals("Hello world", response.getBio());
        assertEquals("Coding, Reading", response.getInterests());
    }
}
