package com.dobrosav.matches.api;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.model.pojo.LoginRequest;
import com.dobrosav.matches.model.pojo.LoginWrapper;
import com.dobrosav.matches.model.pojo.UserRequest;
import com.dobrosav.matches.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(value = "matches/users", method = RequestMethod.POST)
    public User createDefaultUser(@RequestBody UserRequest request) throws Exception {
        long startTime = System.currentTimeMillis();

        log.info("request={}  createDefaultUser executed in {}ms", request, System.currentTimeMillis() - startTime);
        return userService.createDefaultUser(request);
    }

    @RequestMapping(value = "matches/users/{mail}", method = RequestMethod.GET)
    public User findByMail(@PathVariable("mail") String mail) {
        return userService.findByMail(mail);
    }

    @RequestMapping(value = "matches/login/users", method = RequestMethod.POST)
    public ResponseEntity<LoginWrapper> login(@RequestBody LoginRequest request) {
        long startTime = System.currentTimeMillis();
        LoginWrapper wrapper = userService.login(request);
        log.info("request={} result={} login executed in {}ms", request, wrapper, System.currentTimeMillis() - startTime);
        return new ResponseEntity(wrapper, HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUserByAge(@RequestParam(name = "beginYear") Integer begin, @RequestParam(name = "endYear", required = false) Integer end) throws Exception {
        long startTime = System.currentTimeMillis();
        List<User> users = userService.findByAge(begin, end);
        return new ResponseEntity(users, HttpStatus.OK);
    }

}
