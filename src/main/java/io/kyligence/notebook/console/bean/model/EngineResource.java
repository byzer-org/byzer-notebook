package io.kyligence.notebook.console.bean.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EngineResource {
    private long currentJobGroupActiveTasks;
    private long activeTasks;
    private long failedTasks;
    private long completedTasks;
    private long totalTasks;
    private double taskTime;
    private double gcTime;
    private int activeExecutorNum;
    private int totalExecutorNum;
    private int totalCores;
    private long usedMemory;
    private long totalMemory;
    private Object shuffleData;
}