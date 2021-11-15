package io.kyligence.notebook.console.support;

import io.kyligence.notebook.console.exception.AccessDeniedException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.UserService;
import io.kyligence.notebook.console.util.Base64Utils;
import io.kyligence.notebook.console.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Aspect
@Component
public class PermissionChecker {

    @Autowired
    private UserService userService;

    @Around("@annotation(io.kyligence.notebook.console.support.Permission)")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = WebUtils.getRequest();
        HttpServletResponse response = WebUtils.getResponse();

        // 1. check auth header
        String basicAuth = request.getHeader("Authorization");
        if (basicAuth != null) {
            String[] userAndPwd = Base64Utils.getUserAndPwd(basicAuth);

            try {
                userService.auth(userAndPwd[0], userAndPwd[1]);
                WebUtils.setCurrentLoginUser(userAndPwd[0]);
            } catch (Exception e) {
                throw new AccessDeniedException(ErrorCodeEnum.ACCESS_DENIED, e);
            }
            return joinPoint.proceed();
        }

        // 2. check session
        Object sessionUser = request.getSession().getAttribute("username");
        if (sessionUser != null && StringUtils.isNotEmpty(sessionUser.toString())) {
            WebUtils.setCurrentLoginUser(sessionUser.toString());
            return joinPoint.proceed();
        }

        throw new AccessDeniedException(ErrorCodeEnum.ACCESS_DENIED);
    }

}
