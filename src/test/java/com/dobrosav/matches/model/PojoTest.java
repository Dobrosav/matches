package com.dobrosav.matches.model;

import com.dobrosav.matches.api.model.request.*;
import com.dobrosav.matches.api.model.response.*;
import com.dobrosav.matches.db.entities.*;
import com.dobrosav.matches.model.pojo.ApiErrorResponse;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PojoTest {

    private final List<Class<?>> pojoClasses = Arrays.asList(
            LoginRequest.class,
            ProfileUpdateRequest.class,
            ChatMessageRequest.class,
            UserPreferencesRequest.class,
            UserRequest.class,
            UserImageResponse.class,
            ChatMessageResponse.class,
            MatchResponse.class,
            UserResponse.class,
            SuccessResult.class,
            LoginWrapper.class,
            User.class,
            UserLike.class,
            UserDislike.class,
            UserImage.class,
            RefreshToken.class,
            UserMatch.class,
            ChatMessage.class,
            UserPreferences.class,
            ApiErrorResponse.class
    );

    @Test
    public void testGettersAndSetters() throws IntrospectionException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<?> clazz : pojoClasses) {
            Object instance = null;
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // Skip if no default constructor
                System.out.println("Skipping instantiation for " + clazz.getName() + " - no default constructor");
                continue;
            }

            testClassGettersAndSetters(instance);
        }
    }

    private void testClassGettersAndSetters(Object instance) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(instance.getClass()).getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();

            if (readMethod != null && writeMethod != null) {
                Class<?> propertyType = propertyDescriptor.getPropertyType();
                Object testValue = createTestValue(propertyType);

                if (testValue != null) {
                    writeMethod.invoke(instance, testValue);
                    Object retrievedValue = readMethod.invoke(instance);
                    assertEquals(testValue, retrievedValue, "Failed for property: " + propertyDescriptor.getName() + " in class " + instance.getClass().getName());
                }
            }
        }
    }

    private Object createTestValue(Class<?> type) {
        if (type.equals(String.class)) return "testString";
        if (type.equals(Integer.class) || type.equals(int.class)) return 123;
        if (type.equals(Long.class) || type.equals(long.class)) return 123L;
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return true;
        if (type.equals(Date.class)) return new Date();
        if (type.equals(Double.class) || type.equals(double.class)) return 12.34;
        if (type.equals(Float.class) || type.equals(float.class)) return 12.34f;
        if (type.equals(Timestamp.class)) return new Timestamp(System.currentTimeMillis());
        if (type.equals(Instant.class)) return Instant.now();
        if (type.isEnum()) return type.getEnumConstants()[0];
        return null; 
    }

    @Test
    public void testUserConstructorsAndToString() {
        User user = new User("Name", "Surname", "email@test.com", "password", false, false, "M", "username", new Date(), "none");
        assertNotNull(user);
        assertEquals("Name", user.getName());
        assertEquals("email@test.com", user.getEmail());
        assertEquals("", user.getLocation()); 

        User defaultUser = User.createDefaultUser("DefName", "DefSurname", "def@test.com", "defuser", "defpass", "F", new Date(), "none");
        assertNotNull(defaultUser);
        assertFalse(defaultUser.getPremium());
        assertFalse(defaultUser.getAdmin());
        
        // Test toString
        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("User{"));
        assertTrue(toString.contains("username='username'"));
    }

    @Test
    public void testUserResponseConstructors() {
        Date dob = new Date();
        UserResponse resp = new UserResponse(1, "Name", "Surname", "email@test.com", false, false, "M", "username", dob, "none", "loc", "bio", "interests");
        assertEquals(1, resp.getId());
        assertEquals("Name", resp.getName());

        User user = new User("Name", "Surname", "email@test.com", "password", false, false, "M", "username", dob, "none");
        user.setId(1);
        user.setLocation("loc");
        user.setBio("bio");
        user.setInterests("interests");
        
        UserResponse respFromUser = new UserResponse(user);
        assertEquals(user.getId(), respFromUser.getId());
        assertEquals(user.getName(), respFromUser.getName());
        assertEquals(user.getEmail(), respFromUser.getEmail());
    }
    
    @Test
    public void testApiErrorResponse() {
        ApiErrorResponse error = new ApiErrorResponse("100", "Error");
        assertEquals("100", error.getCode());
        assertEquals("Error", error.getMessage());
        
        error.setCode("200");
        error.setMessage("New Error");
        assertEquals("200", error.getCode());
        assertEquals("New Error", error.getMessage());
        
        assertNotNull(error.toString());
    }

    @Test
    public void testRefreshTokenBuilder() {
         Instant now = Instant.now();
         User user = new User();
         
         RefreshToken.RefreshTokenBuilder builder = RefreshToken.builder();
         assertNotNull(builder);
         
         RefreshToken token = builder
                 .id(1L)
                 .token("token")
                 .expiryDate(now)
                 .user(user)
                 .build();
                 
         assertNotNull(token);
         assertEquals(1L, token.getId());
         assertEquals("token", token.getToken());
         assertEquals(now, token.getExpiryDate());
         assertEquals(user, token.getUser());
         
         assertNotNull(builder.toString());
    }

    @Test
    public void testUserMatchConstructor() {
        User u1 = new User();
        u1.setId(1);
        User u2 = new User();
        u2.setId(2);
        
        UserMatch match = new UserMatch(u1, u2);
        assertNotNull(match);
        assertEquals(u1, match.getUser1());
        assertEquals(u2, match.getUser2());
        assertNotNull(match.getCreatedAt());
    }
    
    @Test
    public void testEqualsAndHashCode() {
        // Test RefreshToken equals/hashCode since we saw it overridden
        RefreshToken t1 = new RefreshToken(1L, "token", Instant.MIN, null);
        RefreshToken t2 = new RefreshToken(1L, "token", Instant.MIN, null);
        RefreshToken t3 = new RefreshToken(2L, "other", Instant.MAX, null);
        
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1, t3);
        assertNotEquals(t1, null);
        assertNotEquals(t1, new Object());
    }
}
