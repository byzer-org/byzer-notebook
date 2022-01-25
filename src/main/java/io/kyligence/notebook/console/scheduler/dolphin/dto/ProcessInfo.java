package io.kyligence.notebook.console.scheduler.dolphin.dto;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.kyligence.notebook.console.bean.model.EntityMap;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProcessInfo {
    private int id;
    private String name;
    private int version;
    private String releaseState;
    private int projectId;

    private String processDefinitionJson;
    private String connects;
    private String locations;

    private ProcessDefinition processDefinition;
    private String description;
    private String globalParams;
    private List<String> globalParamList;
    private GlobalParamMap globalParamMap;
    private Date createTime;
    private Date updateTime;
    private String flag;
    private int userId;
    private String userName;
    private String projectName;
    private Map<String, Location> locationMap;
    private List<Connects> connections;
    private String receivers;
    private String receiversCc;
    private String scheduleReleaseState;
    private int timeout;
    private int tenantId;
    private String modifyBy;
    private String resourceIds;


    @Data
    @NoArgsConstructor
    public static class ProcessDefinition {

        private List<String> globalParams;
        private List<Task> tasks;
        private int tenantId;
        private int timeout;

        public static ProcessDefinition valueOf(String entityName,
                                                String entityType, Integer entityId, String commitId,
                                                String taskName, String taskDesc,
                                                String owner, String token, String callbackUrl,
                                                Integer maxRetryTimes, Integer retryInterval,
                                                Integer timeout, Integer tenantId) {
            ProcessDefinition processDefinition = new ProcessDefinition();
            processDefinition.setTimeout(timeout);
            processDefinition.setTenantId(tenantId);
            processDefinition.setGlobalParams(Lists.newArrayList());
            List<Task> tasks = Lists.newArrayList();
            tasks.add(Task.valueOf(
                    entityName, entityType, entityId, commitId, taskName, taskDesc,
                    owner, token, callbackUrl, maxRetryTimes, retryInterval
            ));
            processDefinition.setTasks(tasks);
            return processDefinition;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Task {

        private String type;
        private String id;
        private String name;
        private Params params;
        private String description;
        private Timeout timeout;
        private String runFlag;
        private ConditionResult conditionResult;
        private Dependence dependence;
        private int maxRetryTimes;
        private String retryInterval;
        private String taskInstancePriority;
        private String workerGroup;
        private List<String> preTasks;


        public static String genTaskId(String entityType, Integer entityId) {
            return entityType + "-" + entityId;
        }

        public static String genTaskName(String entityType, String entityName) {
            if (entityType.equalsIgnoreCase("notebook")) {
                return entityName + ".bznb";
            } else if (entityType.equalsIgnoreCase("workflow")){
                return entityName + ".bzwf";
            } else {
                return entityName;
            }
        }

        public static String genUserDefinedTaskName(String userDefinedName, String entityType, String entityName){
            String taskName =  Objects.isNull(userDefinedName) || userDefinedName.isEmpty() ? entityName : userDefinedName;
            return taskName + "#_#" + genTaskName(entityType, entityName);
        }

        public static String fetchEntityName(Task task){
            String[] nameList = task.getName().split("#_#", 2);
            String name = nameList[nameList.length - 1];
            return name.endsWith(".bznb") ? name.replace(".bznb", "") : name.replace(".bzwf", "");
        }

        public static String fetchCommitId(Task task){
            for (HttpParam param: task.getParams().httpParams){
                if (param.getProp().equalsIgnoreCase("commit_id")){
                    return param.getValue();
                }
            }
            return null;
        }

        public static String fetchUserDefinedName(Task task){
            return task.getName().split("#_#", 2)[0];
        }

        public void update(String entityName, String entityType, Integer entityId, String commitId,
                           String taskName, String taskDesc,
                           String owner, String token, String callbackUrl,
                           Integer maxRetryTimes, Integer retryInterval){
            this.setName(genUserDefinedTaskName(taskName, entityType, entityName));
            this.setDescription( Objects.nonNull(taskDesc) ? taskDesc :
                    MessageFormat.format(
                            "Task created by {0} for {1} with id {2} name {3}",
                            owner,
                            entityType,
                            entityId,
                            entityName)
            );
            this.setParams(Params.valueOf(entityType, entityId, commitId, owner, token, callbackUrl));
            this.setMaxRetryTimes(Objects.nonNull(maxRetryTimes) ? maxRetryTimes : 3);
            this.setRetryInterval(Objects.nonNull(retryInterval) ? retryInterval.toString(): "1");

        }

        public static Task valueOf(String entityName,
                                   String entityType, Integer entityId, String commitId,
                                   String taskName, String taskDesc,
                                   String owner, String token, String callbackUrl,
                                   Integer maxRetryTimes, Integer retryInterval) {
            Task task = new Task();
            task.setName(genUserDefinedTaskName(taskName, entityType, entityName));
            task.setId(genTaskId(entityType, entityId));
            task.setType("HTTP");
            task.setDescription( Objects.nonNull(taskDesc) ? taskDesc :
                    MessageFormat.format(
                    "Task created by {0} for {1} with id {2} name {3}",
                    owner,
                    entityType,
                    entityId,
                    entityName)
            );
            task.setTimeout(Timeout.valueOf("", null, false));
            task.setRunFlag("NORMAL");
            task.setConditionResult(ConditionResult.valueOf());
            task.setDependence(new Dependence());
            task.setMaxRetryTimes(Objects.nonNull(maxRetryTimes) ? maxRetryTimes : 3);
            task.setRetryInterval(Objects.nonNull(retryInterval) ? retryInterval.toString(): "1");
            task.setWorkerGroup("default");
            task.setPreTasks(Lists.newArrayList());
            task.setParams(Params.valueOf(entityType, entityId, commitId, owner, token, callbackUrl));
            return task;

        }
    }

    @Data
    @NoArgsConstructor
    public static class Params {

        private List<String> localParams;
        private List<HttpParam> httpParams;
        private String url;
        private String httpMethod;
        private String httpCheckCondition;
        private String condition;
        private int connectTimeout;
        private int socketTimeout;

        public static Params valueOf(String entityType, Integer entityId, String commitId,
                                     String owner, String token, String callbackUrl) {
            Params params = new Params();

            params.setLocalParams(Lists.newArrayList());

            params.setUrl(callbackUrl);
            params.setHttpMethod("POST");
            params.setHttpCheckCondition("STATUS_CODE_DEFAULT");
            params.setCondition("");
            params.setConnectTimeout(60*1000);
            // DolphinScheduler max socketTimout for HTTP Task is 9999999
            params.setSocketTimeout(9999*1000);
            List<HttpParam> paramList = Lists.newArrayList();
            paramList.add(HttpParam.valueOf("entity_type", entityType));
            paramList.add(HttpParam.valueOf("entity_id", entityId.toString()));
            paramList.add(HttpParam.valueOf("owner", owner));
            paramList.add(HttpParam.valueOf("token", token));
            paramList.add(HttpParam.valueOf("commit_id", commitId));
            params.setHttpParams(paramList);
            return params;

        }
    }

    @Data
    @NoArgsConstructor
    public static class Timeout {

        private String strategy;
        private String interval;
        private boolean enable;

        public static Timeout valueOf(String strategy, String interval, boolean enable) {
            Timeout timeout = new Timeout();
            timeout.setEnable(enable);
            timeout.setStrategy(strategy);
            timeout.setInterval(interval);
            return timeout;
        }
    }

    @Data
    @NoArgsConstructor
    public static class HttpParam {

        private String prop;
        private String httpParametersType;
        private String value;

        public static HttpParam valueOf(String prop, String value) {
            HttpParam httpParam = new HttpParam();
            httpParam.setHttpParametersType("BODY");
            httpParam.setProp(prop);
            httpParam.setValue(value);
            return httpParam;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ConditionResult {

        private List<String> successNode;
        private List<String> failedNode;

        public static ConditionResult valueOf() {
            ConditionResult conditionResult = new ConditionResult();
            List<String> list = Lists.newArrayList();
            list.add("");
            conditionResult.setFailedNode(list);
            conditionResult.setSuccessNode(list);
            return conditionResult;
        }

    }

    @Data
    @NoArgsConstructor
    public static class Location {

        private String name;
        private String targetarr;
        private String nodenumber;
        private int x;
        private int y;

        public static Location valueOf(Map<String, Object> o){

            Location loc = new Location();
            loc.setName(o.get("name").toString());
            loc.setNodenumber(o.get("nodenumber").toString());
            loc.setTargetarr(o.get("targetarr").toString());
            loc.setX(Integer.parseInt(o.get("x").toString()));
            loc.setY(Integer.parseInt(o.get("y").toString()));
            return loc;
        }

        public static Location valueOf(String name) {
            Location loc = new Location();
            loc.setName(name);
            loc.setNodenumber("0");
            loc.setTargetarr("");
            return loc;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Connects {

        private String endPointSourceId;
        private String endPointTargetId;

        public static Connects valueOf(String endPointSourceId, String endPointTargetId) {
            Connects connects = new Connects();
            connects.setEndPointSourceId(endPointSourceId);
            connects.setEndPointTargetId(endPointTargetId);
            return connects;
        }
    }

    @Data
    @NoArgsConstructor
    public static class GlobalParamMap {

    }

    @Data
    @NoArgsConstructor
    public static class Dependence {

    }

    public ProcessInfo prepare(){

        processDefinition = JacksonUtils.readJson(processDefinitionJson, ProcessDefinition.class);
        connections = JacksonUtils.readJsonArray(connects, Connects.class);
        Map<String, Object> locMap = (Map<String, Object>) JacksonUtils.readJson(locations, Map.class);
        locationMap = Maps.newHashMap();
        if (Objects.nonNull(locMap)) locMap.forEach((k, v)-> locationMap.put(k, Location.valueOf((Map<String, Object>) v)));
        return this;
    }

    private void deleteFromLocations(String taskId) {
        Set<String> prevTask = connections.stream().filter(c -> c.getEndPointTargetId().equals(taskId))
                .map(Connects::getEndPointSourceId).collect(Collectors.toSet());
        Set<String> nexTask = connections.stream().filter(c -> c.getEndPointSourceId().equals(taskId))
                .map(Connects::getEndPointTargetId).collect(Collectors.toSet());
        for (String prevId : prevTask) {
            Location prevNode = locationMap.get(prevId);
            prevNode.setNodenumber(String.valueOf(Math.max(0, Integer.parseInt(prevNode.getNodenumber()) - 1)));
        }
        for (String nexId : nexTask) {
            Location nexNode = locationMap.get(nexId);
            Set<String> prev = Sets.newHashSet(nexNode.getTargetarr().split(","));
            prev.remove(taskId);
            nexNode.setTargetarr(String.join(",", prev));
        }
        locationMap.remove(taskId);
    }

    private void deleteFromConnects(String taskId) {
        connections = connections.stream().filter(c -> !c.getEndPointTargetId().equals(taskId) && !c.getEndPointSourceId().equals(taskId)).collect(Collectors.toList());
    }


    private void mergeConnects(String taskId, Set<String> prev) {
        List<Connects> newConnects = Lists.newArrayList();
        connections.forEach(c -> {
            if (c.getEndPointTargetId().equals(taskId)) {
                if (prev.contains(c.getEndPointSourceId())) {
                    newConnects.add(c);
                    prev.remove(c.getEndPointSourceId());
                }
            } else {
                newConnects.add(c);
            }
        });
        prev.forEach(sourceId -> newConnects.add(Connects.valueOf(sourceId, taskId)));
        connections = newConnects;
    }

    private void mergeLocations(String taskId, String entityName) {
        Set<String> prevTask = connections.stream().filter(c -> c.getEndPointTargetId().equals(taskId))
                .map(Connects::getEndPointSourceId).collect(Collectors.toSet());

        if (locationMap.containsKey(taskId)) {
            Location loc = locationMap.get(taskId);
            Set<String> prev = Sets.newHashSet();
            if (!loc.getTargetarr().isEmpty()) prev = Arrays.stream(loc.getTargetarr().split(",")).collect(Collectors.toSet());

            loc.setTargetarr(String.join(",", prevTask));


            Set<String> needRemove = Sets.newHashSet(prev);
            needRemove.removeAll(prevTask);
            prevTask.removeAll(prev);

            for (String prevId : needRemove) {
                Location prevNode = locationMap.get(prevId);
                prevNode.setNodenumber(String.valueOf(Math.max(0, Integer.parseInt(prevNode.getNodenumber()) - 1)));
            }
        } else {
            Location loc = Location.valueOf(entityName);
            locationMap.put(taskId, loc);
            loc.setTargetarr(String.join(",", prevTask));
        }

        for (String prevId : prevTask) {
            Location loc = locationMap.get(prevId);

            loc.setNodenumber(loc.getNodenumber().isEmpty() ? "1"  : String.valueOf(Integer.parseInt(loc.getNodenumber()) + 1));
        }
    }

    public static ProcessInfo valueOf(String processName, String description, String entityName,
                                      String entityType, Integer entityId, String commitId,
                                      String taskName, String taskDesc,
                                      String owner, String token, String callbackUrl,
                                      Integer maxRetryTimes, Integer retryInterval,
                                      Integer timeout, Integer tenantId) {
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.setName(processName);
        processInfo.setDescription(description);
        processInfo.setProcessDefinition(ProcessDefinition.valueOf(
                entityName, entityType, entityId,
                commitId, taskName, taskDesc,
                owner, token,
                callbackUrl, maxRetryTimes, retryInterval,
                timeout, tenantId
        ));
        processInfo.setConnections(Lists.newArrayList());
        Map<String, Location> locationMap = Maps.newHashMap();
        locationMap.put(Task.genTaskId(entityType, entityId), Location.valueOf(entityName));
        processInfo.setLocationMap(locationMap);
        return processInfo;
    }

    public void modify(String entityName, String entityType, Integer entityId, String commitId,
                       String taskName, String taskDesc, String owner,
                       String token, String callbackUrl, Integer maxRetryTimes, Integer retryInterval,
                       List<EntityMap> attachTo) {

        if (Objects.isNull(connections)) connections = Lists.newArrayList();
        if (Objects.isNull(locationMap)) locationMap = Maps.newHashMap();

        Set<String> prev = Objects.isNull(attachTo) ? Sets.newHashSet() :
                attachTo.stream().map(e -> Task.genTaskId(e.getEntityType(), e.getEntityId())).collect(Collectors.toSet());
        String taskId = Task.genTaskId(entityType, entityId);


        Task task;
        List<Task> tasks = processDefinition.getTasks();
        Set<String> existTaskNames = tasks.stream().map(Task::fetchUserDefinedName).collect(Collectors.toSet());
        if (!locationMap.containsKey(taskId)) {
            if (existTaskNames.contains(taskName)) {
                throw new ByzerException("Node name:["+ taskName +"] already exist");
            }
            task = Task.valueOf(entityName, entityType, entityId, commitId, taskName, taskDesc, owner, token, callbackUrl, maxRetryTimes, retryInterval);
            tasks.add(task);
            processDefinition.setTasks(tasks);
        } else {
            task = tasks.stream().filter(t -> t.getId().equals(taskId)).collect(Collectors.toList()).get(0);
            existTaskNames.remove(Task.fetchUserDefinedName(task));
            if (existTaskNames.contains(taskName)) {
                throw new ByzerException("Node name:["+ taskName +"] already exist");
            }
            task.update(entityName, entityType, entityId, commitId, taskName, taskDesc, owner, token, callbackUrl, maxRetryTimes, retryInterval);
        }
        task.setPreTasks(new ArrayList<>());
        mergeConnects(taskId, prev);
        mergeLocations(taskId, task.getName());
    }

    public void remove(String entityType, Integer entityId) {

        String taskId = Task.genTaskId(entityType, entityId);
        if (!locationMap.containsKey(taskId)) {
            throw new ByzerException(String.format("%s %s Not Used in %s", entityType, entityId, name));
        }
        List<Task> tasks = processDefinition.getTasks();
        Task task = tasks.stream().filter(t -> t.getId().equals(taskId)).collect(Collectors.toList()).get(0);
        tasks.remove(task);
        deleteFromLocations(taskId);
        deleteFromConnects(taskId);
        processDefinition.setTasks(tasks);
    }

    public boolean contains(String entityType, Integer entityId){
        String id = Task.genTaskId(entityType, entityId);
        for (Task task: processDefinition.getTasks()) {
            if (task.getId().equals(id)) return true;
        }
        return false;
    }

    public static EntityMap getTaskEntityMap(Task task){
        EntityMap r = new EntityMap();
        String[] l = task.getId().split("-");
        r.setEntityId(Integer.parseInt(l[1]));
        r.setEntityType(l[0]);
        r.setCommitId(Task.fetchCommitId(task));
        r.setEntityName(Task.fetchEntityName(task));
        r.setDescription(task.getDescription());
        r.setName(Task.fetchUserDefinedName(task));
        return r;
    }

}
