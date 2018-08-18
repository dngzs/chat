package com.chat.im.core.controller;

import com.chat.im.core.dto.UserDto;
import com.chat.im.core.entity.CtUser;
import com.chat.im.core.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public List<CtUser> get(){
        return userService.getAll();
    }
}
