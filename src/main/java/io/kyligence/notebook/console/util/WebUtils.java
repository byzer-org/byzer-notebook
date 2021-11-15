package io.kyligence.notebook.console.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebUtils {

    private static final String LOGIN_USER_KEY = "username";

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes.getRequest();
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes.getResponse();
    }

    public static void setCurrentLoginUser(String username) {
        HttpServletRequest request = getRequest();
        request.setAttribute(LOGIN_USER_KEY, username.toLowerCase());
    }

    public static String getCurrentLoginUser() {
        HttpServletRequest request = getRequest();
        return (String) request.getAttribute(LOGIN_USER_KEY);
    }
}
