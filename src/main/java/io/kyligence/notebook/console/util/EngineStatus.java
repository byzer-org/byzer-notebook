package io.kyligence.notebook.console.util;

public enum EngineStatus {
    DOWN(0, "down", "Cannot connect to engine"),
    ALIVE(1, "alive", "Engine is alive but not ready"),
    READY(2, "ready", "Engine is alive and ready"),
    BUSY(3, "busy", "Engine is busy"),
    IDLE(4, "idle", "Engine is idle");

    EngineStatus(int code, String val, String message) {
        this.code = code;
        this.value = val;
        this.msg = message;
    }

    private final int code;
    private final String value;
    private final String msg;


    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }

    public static EngineStatus valueOf(Integer statusCode) {
        for (EngineStatus status : values()) {
            if (status.getCode() == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("invalid status : " + statusCode);
    }
}
