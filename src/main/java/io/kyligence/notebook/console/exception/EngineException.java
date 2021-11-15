package io.kyligence.notebook.console.exception;

public class EngineException extends BaseException {

    public EngineException() {

    }

    public EngineException(String msg) {
        super(msg);
    }

    public EngineException(Throwable cause) {
        super(cause);
    }


    public EngineException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EngineException(String format, Object... args) {
        super(format, args);
    }
}
