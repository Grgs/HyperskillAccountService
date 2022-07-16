package com.genspark.account.security;

import com.genspark.account.user.User;
import com.genspark.account.userAuthStatus.UserAuthStatus;
import com.genspark.account.userAuthStatus.UserAuthStatusRepository;
import com.genspark.account.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserAuthStatusRepository userAuthStatusRepository;

    /**
     * Locates the user based on the email. In the actual implementation, the search
     * may be case-sensitive, or case-insensitive depending on how the implementation
     * instance is configured. In this case, the <code>UserDetails</code> object that
     * comes back may have an email that is of a different case than what was actually
     * requested..
     *
     * @param email the email identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        List<User> userList = userRepository.findByEmailIgnoreCase(email);
        if (userList.size() == 0) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        User user = userList.get(0);

        List<UserAuthStatus> userAuthStatusList = userAuthStatusRepository.findByEmailIgnoreCase(email);
        if (userAuthStatusList.size() == 0) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        UserAuthStatus userStatus = userAuthStatusList.get(0);
        return new UserDetailsImpl(user, userStatus);
    }
}
