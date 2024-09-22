package com.dobrosav.matches.service;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.model.pojo.LoginRequest;
import com.dobrosav.matches.model.pojo.LoginWrapper;
import com.dobrosav.matches.model.pojo.SuccessResult;
import com.dobrosav.matches.model.pojo.UserRequest;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @CachePut(value = "users", key = "#request.mail")
    public User createDefaultUser(UserRequest request) throws Exception {
        if (userRepo.findByMail(request.getMail()).isEmpty() && userRepo.findByUsername(request.getSurname()).isEmpty()) {
            User user = User.createDefaultUser(request.getName(), request.getSurname(), request.getMail(), request.getUsername(), request.getPassword(), request.getSex(), request.getDateOfBirth(), request.getDisabilities());
            userRepo.save(user);
            return user;

        } else {
            throw new Exception("User Exist");
        }
    }

    @Cacheable(value = "users", key = "#mail")
    public User findByMail(String mail) {
        return userRepo.findByMail(mail).get(0);
    }

    public LoginWrapper login(LoginRequest request) {
        LoginWrapper loginWrapper = new LoginWrapper();
        User user = userRepo.findByMailAndPassword(request.getMail(), request.getPassword());
        SuccessResult successResult = new SuccessResult();
        if (user != null) {
            successResult.setResult(true);
            loginWrapper.setUser(user);
        } else {
            successResult.setResult(false);
        }
        loginWrapper.setResult(successResult);
        return loginWrapper;
    }

    public List<User> findByAge(Integer begin, Integer end) throws Exception {
        List<User> users;
        if (end != null && end < begin)
            throw new Exception();
        users = userRepo.findAll();
        if (end == null) {
            users = users.stream().filter(user -> Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears() >= begin).toList();
        } else
            users = users.stream().filter(user ->
                            Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears() >= begin)
                    .filter(user -> Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears() <= end).
                    toList();
        return users;
    }
}
