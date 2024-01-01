package com.dobrosav.matches.api;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.model.pojo.LoginRequest;
import com.dobrosav.matches.model.pojo.LoginWrapper;
import com.dobrosav.matches.model.pojo.SuccessResult;
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
    public ResponseEntity<SuccessResult> creaateDefaultUser(@RequestBody UserRequest request) {
        long startTime = System.currentTimeMillis();
        SuccessResult result = userService.createDefaultUser(request);
        log.info("request={} result={} createDefaultUser executed in {}ms", request, result, System.currentTimeMillis() - startTime);
        return new ResponseEntity(result, HttpStatus.OK);
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
        return new ResponseEntity(users,HttpStatus.OK );
    }

}
