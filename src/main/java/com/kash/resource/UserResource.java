package com.kash.resource;

import com.kash.exception.domain.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserResource extends ExceptionHandling {

    @GetMapping("/user")
    public String showUser(){
        return "application works";
    }
}
