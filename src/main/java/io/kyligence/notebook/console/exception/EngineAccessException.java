package io.kyligence.notebook.console.exception;

public class EngineAccessException extends BaseException {

    public EngineAccessException() {

    }

    public EngineAccessException(String msg) {
        super(msg);
    }

    public EngineAccessException(Throwable cause) {
        super(cause);
    }

    public EngineAccessException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getReportMsg());
        this.code = errorCodeEnum.getCode();
    }

    public EngineAccessException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getMsg(), cause);
        this.code = errorCodeEnum.getCode();
    }

    public EngineAccessException(ErrorCodeEnum errorCodeEnum, Object... extensionMessages) {
        super(errorCodeEnum.getReportMsg(), extensionMessages);
        this.code = errorCodeEnum.getCode();
    }

}
