package com.genspark.account.user;

import com.genspark.account.exceptions.UserNotFoundException;
import com.genspark.account.logging.AccountLogger;
import com.genspark.account.logging.LOG_ACTIONS;
import com.genspark.account.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserAuthStatusRepository userAuthStatusRepository;

    @Autowired
    AccountLogger logger;

    @PostMapping("/api/auth/signup")
    Object signup(@Valid @RequestBody User user) {
        user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
        if (userRepository.count() == 0) {
            user.setRoles(List.of(ROLE.ADMINISTRATOR.authority()));
        } else if (userRepository.findByEmailIgnoreCase(user.getEmail()).size() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        } else {
            user.setRoles(List.of(ROLE.USER.authority()));
        }
        validatePassword(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserAuthStatus userStatus = new UserAuthStatus();
        userStatus.setEmail(user.getEmail());
        userStatus.setLocked(false);
        userStatus.setLoginAttempts(0);
        try {
            userAuthStatusRepository.save(userStatus);
            return userRepository.save(user);
        } finally {
            logger.log(LOG_ACTIONS.CREATE_USER, "Anonymous", user.getEmail(), "/api/auth/signup");
        }
    }

    @PostMapping("/api/auth/changepass")
    Map<String, String> changePassword(@Valid @RequestBody NewPassword newPassword,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        String password = newPassword.getNew_password();
        validatePassword(password);
        if (passwordEncoder.matches(password, userDetails.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        User storedUser = getUser(userDetails.getUsername());
        storedUser.setPassword(passwordEncoder.encode(password));
        UserAuthStatus userStatus = this.getUserAuthStatus(userDetails.getUsername());
        userStatus.setLocked(false);
        userStatus.setLoginAttempts(0);
        userAuthStatusRepository.save(userStatus);
        userRepository.save(storedUser);
        logger.log(LOG_ACTIONS.CHANGE_PASSWORD, storedUser.getEmail(), storedUser.getEmail(),
                "/api/auth/changepass");
        Map<String, String> response = new HashMap<>();
        response.put("email", storedUser.getEmail());
        response.put("status", "The password has been updated successfully");
        return response;
    }

    private void validatePassword(String password) {
        if (password.length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        if (BadPasswords.isBad(password))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
    }

    @PutMapping("/api/admin/user/role")
    User setRole(@RequestBody UserRoleInput userRoleInput,
                 @AuthenticationPrincipal UserDetails userDetails) {
        String operation;
        try {
            operation = ROLE_OPERATION.valueOf(userRoleInput.getOperation()).toString();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role operation!");
        }
        String role;
        String authority;
        try {
            role = ROLE.valueOf(userRoleInput.getRole()).toString();
            authority = ROLE.valueOf(role).authority();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }
        User user = getUser(userRoleInput.getUser());
        List<String> currentAuthorities = user.getRoles();
        if (operation.equals(ROLE_OPERATION.GRANT.toString())) {
            if (currentAuthorities.contains(authority)) {
                return user;
            }
            if (role.equals(ROLE.ADMINISTRATOR.toString()) || currentAuthorities.contains(ROLE.ADMINISTRATOR.authority())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The user cannot combine administrative and business roles!");
            }
            currentAuthorities.add(authority);
        } else { // operation == REMOVE;
            if (!currentAuthorities.contains(authority))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
            if (role.equals(ROLE.ADMINISTRATOR.toString()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            if (currentAuthorities.size() < 2)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
            currentAuthorities.remove(authority);
        }
        user.setRoles(currentAuthorities.stream().sorted().collect(Collectors.toList()));
        try {
            return userRepository.save(user);
        } finally {
            logger.log(operation.equals("GRANT") ? LOG_ACTIONS.GRANT_ROLE : LOG_ACTIONS.REMOVE_ROLE,
                    userDetails.getUsername(), String.format("%s%s role %s %s %s", operation.charAt(0),
                            operation.substring(1).toLowerCase(), role,
                            operation.equals(ROLE_OPERATION.GRANT.toString()) ? "to" : "from", user.getEmail()),
                    "/api/admin/user/role");
        }
    }

    @DeleteMapping("/api/admin/user/{email}")
    Map<String, String> deleteUser(@PathVariable String email, @AuthenticationPrincipal UserDetails userDetails) {
        User foundUser = getUser(email);
        if (foundUser.getRoles().contains(ROLE.ADMINISTRATOR.authority()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        userRepository.deleteByEmailIgnoreCase(email);
        userAuthStatusRepository.deleteByEmailIgnoreCase(email);
        logger.log(LOG_ACTIONS.DELETE_USER, userDetails.getUsername(), foundUser.getEmail(),
                "/api/admin/user");
        Map<String, String> response = new HashMap<>();
        response.put("user", email);
        response.put("status", "Deleted successfully!");
        return response;
    }

    @GetMapping("/api/admin/user")
    List<User> getUser() {
        return userRepository.findAll();
    }

    @PutMapping("/api/admin/user/access")
    Map<String, String> lockUser(@RequestBody UserLockInput userLockInput,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        String operation;
        try {
            operation = LOCK_OPERATION.valueOf(userLockInput.getOperation()).toString();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid lock operation!");
        }
        User user = getUser(userLockInput.getUser());
        UserAuthStatus userStatus = this.getUserAuthStatus(userLockInput.getUser());
        if (user.getRoles().contains(ROLE.ADMINISTRATOR.authority())
                && operation.equals(LOCK_OPERATION.LOCK.toString()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        if (operation.equals(LOCK_OPERATION.UNLOCK.toString())) {
            userStatus.setLocked(false);
            userStatus.setLoginAttempts(0);
        } else {
            userStatus.setLocked(true);
        }
        userAuthStatusRepository.save(userStatus);
        if (userStatus.isLocked()) {
            logger.log(LOG_ACTIONS.LOCK_USER, userStatus.getEmail(), "Lock user " + userStatus.getEmail(),
                    "/api/admin/user/access");
        } else {
            logger.log(LOG_ACTIONS.UNLOCK_USER, userDetails.getUsername(), "Unlock user " + userStatus.getEmail(),
                    "/api/admin/user/access");
        }
        return Map.of("status", String.format("User %s %s!", userStatus.getEmail(),
                userStatus.isLocked() ? "locked" : "unlocked"));
    }

    private User getUser(String email) {
        List<User> userList = userRepository.findByEmailIgnoreCase(email);
        if (userList.size() == 0) {
            throw new UserNotFoundException();
        }
        return userList.get(0);
    }

    private UserAuthStatus getUserAuthStatus(String email) {
        List<UserAuthStatus> userList = userAuthStatusRepository.findByEmailIgnoreCase(email);
        if (userList.size() == 0) {
            throw new UserNotFoundException();
        }
        return userList.get(0);
    }
}
