package com.kash.service.serviceImpl;

import com.kash.domain.User;
import com.kash.domain.UserPrincipal;
import com.kash.exception.domain.EmailExistException;
import com.kash.exception.domain.EmailNotFoundException;
import com.kash.exception.domain.UsernameExistException;
import com.kash.repository.UserRepository;
import com.kash.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@Qualifier("UserDetailService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


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

    public User register(String firstName, String lastName, String username, String email) throws EmailNotFoundException, EmailExistException {
        validNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        return null;
    }

    private User validNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws EmailNotFoundException, EmailExistException {
        if(StringUtils.isNotBlank(currentUsername)){
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null){
                throw new UsernameNotFoundException("No user found by username" + currentUsername);
            }
            User userByUsername = findUserByUsername(currentUsername);
            if(userByUsername != null && currentUser.getId().equals(userByUsername.getId())){
                throw new UsernameExistException("Username already exist!");
            }

            User userByEmail = findUserByEmail(newEmail);
            if)(userByEmail != null && currentUser.getId().equals(userByEmail.getId())){
                throw new EmailExistException("Username already exist!");
            }
            return currentUser;
        } else {
            User userByUsername = findUserByUsername(newUsername);
            if(userByUsername != null){
                throw new UsernameExistException("Username already exists")
            }

            User userByEmail = findUserByEmail(newEmail);
            if)(userByEmail != null){
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
