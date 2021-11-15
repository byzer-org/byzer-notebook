package io.kyligence.notebook.console.exception;

import io.kyligence.notebook.console.util.MessageFormatter;

public class BaseException extends RuntimeException {

    protected String code = "KN-1000001";

    public String getCode() {
        return this.code;
    }

    protected BaseException() {

    }

    protected BaseException(String msg) {
        super(msg);
    }

    protected BaseException(Throwable cause) {
        super(cause);
    }


    protected BaseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    protected BaseException(String format, Object... args) {
        super(msg(format, args), ex(format, args));
    }

    private static String msg(String format, Object... args) {
        MessageFormatter.FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        return ft.getMessage();
    }

    private static Throwable ex(Object... args) {
        return MessageFormatter.getThrowableCandidate(args);
    }
}
