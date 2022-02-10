package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.bean.dto.UserInfoDTO;
import io.kyligence.notebook.console.bean.dto.UserRegisterAndResetDTO;
import io.kyligence.notebook.console.bean.dto.req.UserJoinReq;
import io.kyligence.notebook.console.bean.entity.UserInfo;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.UserService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.Base64Utils;
import io.kyligence.notebook.console.support.EncryptUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotebookHelper notebookHelper;


    @ApiOperation("User Login")
    @PostMapping("/user/authentication")
    public Response<Integer> login(@RequestHeader("Authorization") String basicAuth,
                                   HttpServletRequest request) {
        String[] userAndPwd = Base64Utils.getUserAndPwd(basicAuth);

        Integer userId = userService.auth(userAndPwd[0], userAndPwd[1]);
        request.getSession().setAttribute("username", userAndPwd[0]);
        return new Response<Integer>().data(userId);
    }

    @ApiOperation("User Sign Up")
    @PostMapping("/user/join")
    @SneakyThrows
    public Response<Integer> signUp(@RequestBody @Validated UserJoinReq userJoinReq) {
        String user = userJoinReq.getUsername();

        UserInfo userInfo = new UserInfo();
        userInfo.setName(user);
        userInfo.setPassword(EncryptUtils.encrypt(userJoinReq.getPassword()));
        userInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));

        userService.createUser(userInfo);
        WebUtils.setCurrentLoginUser(user);

        postSignup(user);

        return new Response<Integer>().data(userInfo.getId());
    }


    @ApiOperation("reset password when not login")
    @PostMapping("/user/reset_password")
    public Integer changePasswordWithHeader(HttpServletRequest request) {
        String header = request.getHeader("reset-password");
        UserRegisterAndResetDTO registerDTO = JacksonUtils.readJson(EncryptUtils.decrypt(header), UserRegisterAndResetDTO.class);
        String userName = registerDTO.getName();
        UserInfo userInfo = userService.findUserByName(userName);

        if (userInfo == null) {
            throw new ByzerException(ErrorCodeEnum.ACCESS_DENIED);
        }

        userInfo.setPassword(registerDTO.getPassword());
        userService.updateUser(userInfo);
        return userInfo.getId();
    }

    @ApiOperation("user reset password")
    @PostMapping("/user/reset")
    @Permission
    public Response<Integer> changePassword(@RequestBody @Validated UserJoinReq userJoinReq) {
        String user = WebUtils.getCurrentLoginUser();
        if (user != null && !user.equals(userJoinReq.getUsername())) {
            throw new ByzerException(ErrorCodeEnum.ACCESS_DENIED);
        }
        UserInfo userInfo = userService.findUserByName(user);
        userInfo.setPassword(EncryptUtils.encrypt(userJoinReq.getPassword()));
        userService.updateUser(userInfo);
        return new Response<Integer>().data(userInfo.getId());
    }

    @Async
    protected void postSignup(String user) {
        log.info("skip create sample for: " + user);
        // notebookHelper.createSampleDemo(user);
    }


    @ApiOperation("Get User Info")
    @GetMapping("/user/me")
    @Permission
    public Response<UserInfoDTO> getUserInfo() {
        String username = WebUtils.getCurrentLoginUser();
        UserInfo userInfo = userService.findUserByName(username);

        return new Response<UserInfoDTO>().data(UserInfoDTO.valueOf(userInfo));
    }

    @ApiOperation("user logout")
    @DeleteMapping("/user/authentication")
    @Permission
    public Response<String> logout(HttpServletRequest request) {
        String username = WebUtils.getCurrentLoginUser();
        request.getSession().setAttribute("username", "");
        return new Response<String>().data(username);
    }

    @ApiOperation("Register user")
    @PostMapping("/user/register")
    @Transactional
    public Integer register(HttpServletRequest request) {

        String header = request.getHeader("registration");
        UserRegisterAndResetDTO registerDTO = JacksonUtils.readJson(EncryptUtils.decrypt(header), UserRegisterAndResetDTO.class);
        String user = registerDTO.getName();

        UserInfo userInfo = new UserInfo();
        userInfo.setName(user);
        userInfo.setPassword(registerDTO.getPassword());
        userInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));

        userService.createUser(userInfo);
        WebUtils.setCurrentLoginUser(user);

        postSignup(user);

        return userInfo.getId();
    }


}
