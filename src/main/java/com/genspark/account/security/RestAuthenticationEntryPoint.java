package com.genspark.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genspark.account.logging.AccountLogger;
import com.genspark.account.user.UserAuthStatus;
import com.genspark.account.user.UserAuthStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    AccountLogger accountLogger;
    @Autowired
    UserAuthStatusRepository userAuthStatusRepository;

    /**
     * Commences an authentication scheme.
     * <p>
     * <code>ExceptionTranslationFilter</code> will populate the <code>HttpSession</code>
     * attribute named
     * <code>AbstractAuthenticationProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY</code>
     * with the requested target URL before calling this method.
     * <p>
     * Implementations should modify the headers on the <code>ServletResponse</code> as
     * necessary to commence the authentication process.
     *
     * @param request       that resulted in an <code>AuthenticationException</code>
     * @param response      so that the user agent can begin authentication
     * @param authException that caused the invocation
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String email;
        try {
            email = new String(Base64.getDecoder().decode(request.getHeader("authorization")
                    .split(" ")[1])).split(":")[0];
        } catch (Exception e) {
            email = request.getRemoteUser();
        }
        List<UserAuthStatus> userStatusList = userAuthStatusRepository.findByEmailIgnoreCase(email);
        UserAuthStatus userStatus;
        if (userStatusList.size() == 0) {
            userStatus = null;
        } else {
            userStatus = userStatusList.get(0);
        }
        String message;
        if (userStatus == null)
            message = "Access Denied!";
        else if (userStatus.isLocked() || userStatus.getLoginAttempts() > 5)
            message = "User account is locked";
        else
            message = "Access Denied!";

        try {
            ObjectMapper mapper = new ObjectMapper();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("timestamp", new Timestamp(System.currentTimeMillis()).toString());
            responseMap.put("status", 401);
            responseMap.put("error", "Unauthorized");
            responseMap.put("message", message);
            responseMap.put("path", request.getServletPath());
            response.getWriter().write(mapper.writeValueAsString(responseMap));
        } catch (Exception e1) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied!");
        }
    }
}
