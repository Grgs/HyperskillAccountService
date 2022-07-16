package com.genspark.account.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Handles an access denied failure.
     *
     * @param request               that resulted in an <code>AccessDeniedException</code>
     * @param response              so that the user agent can be advised of the failure
     * @param accessDeniedException that caused the invocation
     * @throws IOException      in the event of an IOException
     * @throws ServletException in the event of a ServletException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
//        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied!");
//        PrintWriter responseWriter = response.getWriter();
//        Map<String, Object> responseMap = new HashMap<>();
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        responseMap.put("timestamp", new Timestamp(System.currentTimeMillis()).toString());
//        responseMap.put("status", 403);
//        responseMap.put("error", HttpStatus.FORBIDDEN.toString());
//        responseMap.put("message", "Access Denied!");
//        responseMap.put("path", request.getServletPath());
//        response.setStatus(403);
//        responseWriter.print(new ObjectMapper().writeValueAsString(responseMap));
//        response.flushBuffer();


//        logger.log(LOG_ACTIONS.ACCESS_DENIED, request.getRemoteUser(), request.getRequestURI(),
//                request.getRequestURI());

    }
}
