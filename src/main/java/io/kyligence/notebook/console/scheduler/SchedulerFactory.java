package io.kyligence.notebook.console.scheduler;

import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.scheduler.dolphin.DolphinScheduler;

public class SchedulerFactory {
    interface SchedulerType {
        String Dolphin = "dolphinscheduler";
    }

    public static RemoteSchedulerInterface create(Integer id, SchedulerConfig conf) {
        switch (conf.getSchedulerName().toLowerCase()) {
            case SchedulerType.Dolphin:
                return new DolphinScheduler(id, conf);
            default:
                throw new ByzerException(String.format("Scheduler: [%s] not support yet.", conf.getSchedulerName()));
        }
    }
}
