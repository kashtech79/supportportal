package com.kash.service.serviceImpl;

import com.kash.domain.User;
import com.kash.domain.UserPrincipal;
import com.kash.exception.domain.EmailExistException;
import com.kash.exception.domain.EmailNotFoundException;
import com.kash.exception.domain.UsernameExistException;
import com.kash.repository.UserRepository;
import com.kash.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.kash.enumeration.Role.ROLE_USER;

@Service
@Transactional
@Qualifier("UserDetailService")
public class UserServiceImpl implements UserService, UserDetailsService {
    //1
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    //8
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    //2
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            LOGGER.error("User not found by username: " + username);
            throw new UsernameNotFoundException("User doesn't exist: "  + username);
        }else {
            user.setLastLoginDateDisplay((user.getLastLoginDate()));
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning found user by username:" + username);
            return userPrincipal;
        }
    }

    public User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException {
        //3
        validNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        //5
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl());
        userRepository.save(user);
        LOGGER.info("New user password: " + password);
//        emailService.sendNewPasswordEmail(firstName, password, email);
//        return user;
        return user;
    }

    //10
    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
    }

    //9
    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }


    //7
    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    //6
    private String generateUserId(){
        return RandomStringUtils.randomNumeric(10);
    }

    //4
    private User validNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws EmailExistException, UsernameExistException {
        if(StringUtils.isNotBlank(currentUsername)){
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null){
                throw new UsernameNotFoundException("No user found by username" + currentUsername);
            }
            User userByNewUsername = findUserByUsername(currentUsername);
            if(userByNewUsername != null && currentUser.getId().equals(userByNewUsername.getId())){
                throw new UsernameExistException("Username already exist!");
            }

            User userByNewEmail = findUserByEmail(newEmail);
            if(userByNewEmail != null && currentUser.getId().equals(userByNewEmail.getId())){
                throw new EmailExistException("Username already exist!");
            }
            return currentUser;
        } else {
            User userByUsername = findUserByUsername(newUsername);
            if(userByUsername != null){
                throw new UsernameExistException("Username already exists");
            }

            User userByEmail = findUserByEmail(newEmail);
            if(userByEmail != null){
                throw new EmailExistException("Username already exist!");
            }
            return null;
        }
    }

    public List<User> getUsers() {
        return null;
    }

    public User findUserByUsername(String username) {
        return null;
    }

    public User findUserByEmail(String email) {
        return null;
    }
}
