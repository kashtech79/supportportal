package com.kash.resource;

import com.kash.exception.domain.EmailExistException;
import com.kash.exception.domain.ExceptionHandling;
import com.kash.exception.domain.UserNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserResource extends ExceptionHandling {

    @GetMapping("/home")
    public String showUser() throws UserNotFoundException {
        throw new UserNotFoundException("User doesn't exist");
    }
}
