package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.security.SecurityUtils;
import jakarta.validation.Valid;

import com.techelevator.tenmo.exception.DaoException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserDao userDao;

    //TODO: Never used
    private JdbcUserDao jdbcUserDao;



    public UserController(UserDao userDao){
        this.userDao = userDao;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id){

        return userDao.getUserById(id);
    }

    @GetMapping("")
    public List<User> userList(){
        List<User> userList;
        userList = userDao.getUsers();
        return userList;
    }


    @GetMapping("/{id}/username")
    public List <User> getUserIdAndUsername(){
        List<User> userIdAndName = userDao.getUserIdAndName();
        return userIdAndName;
    }

    @GetMapping("/me")
    public User getUserByUsername(Principal principal) {
        String username = principal.getName();

        User user = userDao.getUserByUsername(username);

        return user;
    }

    @GetMapping("/username/account/{accountId}")
    public String getUsernameByAccountId(@PathVariable int accountId) {

        return userDao.getUsernameByAccountId(accountId);
    }


    }
