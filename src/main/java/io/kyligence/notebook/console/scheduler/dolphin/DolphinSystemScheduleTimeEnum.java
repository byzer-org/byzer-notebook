package io.kyligence.notebook.console.scheduler.dolphin;

import java.util.HashMap;
import java.util.Map;

public enum DolphinSystemScheduleTimeEnum {
    SYSTEM_BIZ_DATE("system.biz.date", "IN", "VARCHAR", "${system.biz.date}"),
    SYSTEM_BIZ_CURDATE("system.biz.curdate", "IN", "VARCHAR", "${system.biz.curdate}"),
    SYSTEM_DATETIME("system.datetime", "IN", "VARCHAR", "${system.datetime}"),
    ;

    DolphinSystemScheduleTimeEnum(String prop, String direct, String type, String value) {
        this.prop = prop;
        this.direct = direct;
        this.type = type;
        this.value = value;
    }

    private final String prop;
    private final String direct;
    private final String type;
    private final String value;

    public Map<String, String> getProp() {
        Map<String, String> map = new HashMap<>();
        map.put("prop", prop);
        map.put("direct", direct);
        map.put("type", type);
        map.put("value", value);
        return map;
    }

}
