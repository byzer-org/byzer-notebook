package io.kyligence.notebook.console.exception;

public class ByzerIgnoreException extends BaseException{

    public ByzerIgnoreException() {
    }

    public ByzerIgnoreException(String message) {
        super(message);
        this.code = ErrorCodeEnum.UNKNOWN_ERROR.getCode();
    }

    public ByzerIgnoreException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getReportMsg());
        this.code = errorCodeEnum.getCode();
    }

    public ByzerIgnoreException(ErrorCodeEnum errorCodeEnum, Object... extensionMessages) {
        super(errorCodeEnum.getReportMsg(), extensionMessages);
        this.code = errorCodeEnum.getCode();
    }

    public ByzerIgnoreException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getReportMsg(), cause);
        this.code = errorCodeEnum.getCode();
    }

    public ByzerIgnoreException(BaseException e){
        super(e.getMessage());
        this.code = e.getCode();
    }

    @Override
    public String toString() {
        return "[" + code + "]" + this.getMessage();
    }
}
