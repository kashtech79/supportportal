package com.kash.service;

import com.kash.domain.User;
import com.kash.exception.domain.EmailExistException;
import com.kash.exception.domain.EmailNotFoundException;
import com.kash.exception.domain.UserNotFoundException;
import com.kash.exception.domain.UsernameExistException;

import javax.mail.MessagingException;
import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException, UserNotFoundException, MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);


}
