package com.dobrosav.matches.mapper;

import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getMail(),
                user.getPremium(),
                user.getAdmin(),
                user.getSex(),
                user.getUsername(),
                user.getDateOfBirth(),
                user.getDisability(),
                user.getLocation(),
                user.getBio(),
                user.getInterests()
        );
    }
}
