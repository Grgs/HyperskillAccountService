package com.genspark.account.security;

import com.genspark.account.logging.AccountLogger;
import com.genspark.account.logging.LOG_ACTIONS;
import com.genspark.account.user.UserAuthStatus;
import com.genspark.account.user.UserAuthStatusRepository;
import com.genspark.account.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.List;

@Component
public class AuthenticationListeners {
    @Autowired
    UserAuthStatusRepository userAuthStatusRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountLogger accountLogger;
    @Autowired
    HttpServletRequest request;

    @EventListener
    public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {
        String email = (String) event.getAuthentication().getPrincipal();
        accountLogger.log(LOG_ACTIONS.LOGIN_FAILED, email, request.getRequestURI(),
                request.getRequestURI());
        UserAuthStatus userStatus = this.getUserAuthStatus(email);
        if (userStatus != null) {
            userStatus.setLoginAttempts(userStatus.getLoginAttempts() + 1);
            if (userStatus.getLoginAttempts() > 4 && !userStatus.isLocked()) {
                accountLogger.log(LOG_ACTIONS.BRUTE_FORCE, userStatus.getEmail(), request.getRequestURI(),
                        request.getRequestURI());
                if (!userRepository.findByEmailIgnoreCase(userStatus.getEmail()).get(0).getRoles().contains(
                        ROLE.ADMINISTRATOR.authority())) {
                    userStatus.setLocked(true);
                    accountLogger.log(LOG_ACTIONS.LOCK_USER, userStatus.getEmail(),
                            "Lock user " + userStatus.getEmail(), request.getRequestURI());
                }
            }
            userAuthStatusRepository.save(userStatus);
        }
    }

    @EventListener
    public void authenticationSucceeded(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        UserAuthStatus userStatus = this.getUserAuthStatus(email);
        if (userStatus == null)
            throw new RuntimeException();
        userStatus.setLoginAttempts(0);
        userAuthStatusRepository.save(userStatus);
    }

    @EventListener
    public void accessDenied(AuthorizationFailureEvent event) {
        String email;
        try {
            email = new String(Base64.getDecoder().decode(request.getHeader("authorization")
                    .split(" ")[1])).split(":")[0];
        } catch (Exception e) {
            email = request.getRemoteUser();
        }
//        accountLogger.log(LOG_ACTIONS.ACCESS_DENIED, email, request.getRequestURI(), request.getRequestURI());
        String path = request.getServletPath();
        if (path == null)
            path = request.getRequestURI();

        if (!(path.equals("/error") || email == null)) {
            accountLogger.log(LOG_ACTIONS.ACCESS_DENIED, email, path, path);
        }
    }

    private UserAuthStatus getUserAuthStatus(String email) {
        List<UserAuthStatus> userAuthStatusList = userAuthStatusRepository.findByEmailIgnoreCase(email);
        if (userAuthStatusList.size() == 0) {
            return null;
        }
        return userAuthStatusList.get(0);
    }
}
