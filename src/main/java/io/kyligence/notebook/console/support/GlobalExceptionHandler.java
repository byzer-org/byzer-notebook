package io.kyligence.notebook.console.support;

import com.google.common.base.Throwables;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.exception.*;
import io.kyligence.notebook.console.util.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


@Slf4j
@ControllerAdvice("io.kyligence.notebook.console.controller")
public class GlobalExceptionHandler {

    //    private static final int FIND_SPECIFIC_EXCEPTION_LOOP = 10;
    private static final String nl = System.getProperty("line.separator");

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<String> handle(AccessDeniedException e) {
        String errorMsg = e.getMessage();
        String code = e.getCode();
        log.error(errorMsg, e);
        return getResponse(errorMsg, code, e, false);
    }

    @ExceptionHandler(MethodDisabledException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<String> handle(MethodDisabledException e){
        String code = ErrorCodeEnum.ACCESS_DISABLED_API.getCode();
        log.error("Disabled method called", e);
        return getResponse("Not Found", code, e,false);
    }

    @ExceptionHandler(DataAccessException.class)
    public Response handle(DataAccessException e) {
        log.error(ExceptionUtils.getRootCause(e), e);
        throw ByzerException.valueOf(e);
    }

    @ExceptionHandler(EngineAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<String> handle(EngineAccessException e) {
        String errorMsg = ExceptionUtils.getRootCause(e);
        String code = e.getCode();
        log.error(errorMsg, e);
        return getResponse(errorMsg, code, e, false);
    }

    @ExceptionHandler(ByzerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<String> handle(ByzerException e) {
        log.error(ExceptionUtils.getRootCause(e), e);
        String code = e.getCode();
        String msg = e.getMessage();
        return getResponse(msg, code, e, false);
    }

    @ExceptionHandler(ByzerIgnoreException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Response<String> handle(ByzerIgnoreException e) {
        log.error(ExceptionUtils.getRootCause(e), e);
        String code = e.getCode();
        String msg = e.getMessage();
        return getResponse(msg, code, e, false);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<String> handle(Exception e) {
        String errorMsg = ExceptionUtils.getRootCause(e);
        String code = ErrorCodeEnum.UNKNOWN_ERROR.getCode();
        log.error(errorMsg, e);
        return getResponse(errorMsg, code, e, false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<String> handle(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String code = ErrorCodeEnum.METHOD_ARG_NOT_VALID.getCode();
        return getResponse(errorMsg, code, e, false);
    }



    private Response<String> getResponse(String msg, String code, Throwable e, boolean detail) {
        if (detail) {
            msg = msg + nl + Throwables.getStackTraceAsString(e);
        }
        return new Response<String>().code(code)
                .data("Request Failed")
                .msg(msg);
    }
}