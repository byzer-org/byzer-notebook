package io.kyligence.notebook.console.exception;

import org.springframework.dao.DataAccessException;

public class ByzerException extends BaseException {

    public ByzerException() {
    }

    public ByzerException(String message) {
        super(message);
        this.code = ErrorCodeEnum.UNKNOWN_ERROR.getCode();
    }

    public ByzerException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getReportMsg());
        this.code = errorCodeEnum.getCode();
    }

    public ByzerException(ErrorCodeEnum errorCodeEnum, Object... extensionMessages) {
        super(errorCodeEnum.getReportMsg(), extensionMessages);
        this.code = errorCodeEnum.getCode();
    }

    public ByzerException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getReportMsg(), cause);
        this.code = errorCodeEnum.getCode();
    }

    public static ByzerException valueOf(DataAccessException e){
        return new ByzerException(ErrorCodeEnum.DATA_ACCESS_FAILED, e);
    }

    @Override
    public String toString() {
        return "[" + code + "]" + this.getMessage();
    }
}
