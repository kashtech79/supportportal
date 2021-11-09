package com.kash.service;

import com.kash.domain.User;
import com.kash.exception.domain.EmailExistException;
import com.kash.exception.domain.EmailNotFoundException;
import com.kash.exception.domain.UserNotFoundException;
import com.kash.exception.domain.UsernameExistException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException, UserNotFoundException, MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException);

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException;

    void deleteUser(String username);

    void resetPassword(String email) throws MessagingException;

    User updateProfileImage(String username, MultipartFile profileImage) throws EmailExistException, UsernameExistException;

}
