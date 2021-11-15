package io.kyligence.notebook.console.exception;

public class AccessDeniedException extends BaseException {

    public AccessDeniedException() {

    }

    public AccessDeniedException(String msg) {
        super(msg);
    }

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    public AccessDeniedException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getReportMsg());
        this.code = errorCodeEnum.getCode();
    }

    public AccessDeniedException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getMsg(), cause);
        this.code = errorCodeEnum.getCode();
    }

    public AccessDeniedException(ErrorCodeEnum errorCodeEnum, Object... extensionMessages) {
        super(errorCodeEnum.getReportMsg(), extensionMessages);
        this.code = errorCodeEnum.getCode();
    }


}
