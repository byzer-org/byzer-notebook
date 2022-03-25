package io.kyligence.notebook.console.scheduler.dolphin;

public enum DolphinInstanceCommandEnum {


    RESUME(1, "RECOVER_SUSPENDED_PROCESS"),
    RECOVER(2, "START_FAILURE_TASK_PROCESS"),
    REPEAT(3, "REPEAT_RUNNING"),
    PAUSE(4, "PAUSE"),
    STOP(5, "STOP");

    DolphinInstanceCommandEnum(int code, String val) {
        this.code = code;
        this.value = val;
    }

    private final int code;

    private final String value;

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static DolphinInstanceCommandEnum valueOf(Integer status) {
        for (DolphinInstanceCommandEnum cmdType : values()) {
            if (cmdType.getCode() == status) {
                return cmdType;
            }
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }
}
