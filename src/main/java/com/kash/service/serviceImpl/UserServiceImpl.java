package com.kash.service.serviceImpl;

import com.kash.domain.User;
import com.kash.domain.UserPrincipal;
import com.kash.enumeration.Role;
import com.kash.exception.domain.EmailExistException;
import com.kash.exception.domain.UserNotFoundException;
import com.kash.exception.domain.UsernameExistException;
import com.kash.repository.UserRepository;
import com.kash.service.UserService;
import javassist.bytecode.MethodParametersAttribute;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.kash.constant.UserImplConstant.*;
import static com.kash.enumeration.Role.ROLE_USER;
import static jdk.internal.joptsimple.internal.Strings.EMPTY;

@Service
@Transactional
@Qualifier("UserDetailService")
public class UserServiceImpl implements UserService, UserDetailsService {


    //1
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    //8
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    //
    private LoginAttemptService loginAttemptService;
    private EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, LoginAttemptService loginAttemptService, EmailService emailService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    //2
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            LOGGER.error(NO_USER_FOUND_BY_USERNAME + username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME  + username);
        }else {
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay((user.getLastLoginDate()));
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info(FOUND_USER_BY_USERNAME + username);
            return userPrincipal;
        }
    }

    private void validateLoginAttempt(User user) {
        if(user.isNotLocked()){
            if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())){
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }

        }else{
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    public User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException, MessagingException {
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
//        LOGGER.info("New user password: " + password);
        emailService.sendNewPasswordEmail(firstName, password, email);
        return user;
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException {
        validNewUsernameAndEmail(EMPTY, username, email);
        User user = new User();
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        saveProfileImage(user, profileImage);
        LOGGER.info("New user password: " + password);
        return user;

    }

    private void saveProfileImage(User user, MultipartFile profileImage) {
    }

    private Role getRoleEnumName(String role) {
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) {
        return null;
    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void resetPassword(String email) {

    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) {
        return null;
    }

    //10
    private String getTemporaryProfileImageUrl(String username) {
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
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        if(StringUtils.isNotBlank(currentUsername)){
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null){
                throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }

            if(userByNewUsername != null && currentUser.getId().equals(userByNewUsername.getId())){
                throw new UsernameExistException(USERNAME_ALREADY_EXIST);
            }


            if(userByNewEmail != null && currentUser.getId().equals(userByNewEmail.getId())){
                throw new EmailExistException(EMAIL_ALREADY_EXIST);
            }
            return currentUser;
        } else {
            if(userByNewUsername != null){
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }

            if(userByNewEmail != null){
                throw new EmailExistException(EMAIL_ALREADY_EXIST);
            }
            return null;
        }
    }

    public List<User> getUsers() {

        return userRepository.findAll();
    }

    public User findUserByUsername(String username) {

        return userRepository.findUserByUsername(username);
    }

    public User findUserByEmail(String email) {

        return userRepository.findUserByEmail(email);
    }
}
