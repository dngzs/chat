package com.chat.im.core.controller;

import com.chat.im.core.dto.UserDto;
import com.chat.im.core.entity.CtUser;
import com.chat.im.core.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class TestController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String get(){
        return "index.html";
    }
}
