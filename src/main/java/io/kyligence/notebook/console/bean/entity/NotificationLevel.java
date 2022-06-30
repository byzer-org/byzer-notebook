package io.kyligence.notebook.console.bean.entity;

import io.kyligence.notebook.console.util.EngineStatus;

/**
 * @Author; Andie Huang
 * @Date: 2022/6/29 22:51
 */
public enum NotificationLevel {
    ALL(0, "all", "sending IM whenever success or failed"),
    FAILED(1, "failed", "Sending IM when job is failed");

    NotificationLevel(int code, String val, String message) {
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

    public static NotificationLevel valueByLevel(String level) {
        for (NotificationLevel status : values()) {
            if (status.getValue().equalsIgnoreCase(level)) {
                return status;
            }
        }
        throw new IllegalArgumentException("invalid level : " + level);
    }

}
