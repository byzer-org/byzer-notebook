package io.kyligence.notebook.console.service;

import com.google.common.collect.Lists;
import io.kyligence.notebook.console.bean.dto.ETNodeDTO;
import io.kyligence.notebook.console.bean.dto.ETParamDTO;
import io.kyligence.notebook.console.bean.dto.req.DynamicDependsReq;
import io.kyligence.notebook.console.bean.dto.req.RegisterETDTO;
import io.kyligence.notebook.console.bean.entity.ETParamsDef;
import io.kyligence.notebook.console.bean.entity.RegisterET;
import io.kyligence.notebook.console.bean.entity.UsageTemplate;
import io.kyligence.notebook.console.bean.model.ETParamResp;
import io.kyligence.notebook.console.bean.model.ParamConstraint;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import io.kyligence.notebook.console.dao.ETParamsDefRepository;
import io.kyligence.notebook.console.dao.RegisterETRepository;
import io.kyligence.notebook.console.dao.UsageTemplateRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.util.ETParamUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ETService {

    @Autowired
    private EngineService engineService;

    @Autowired
    private RegisterETRepository etRepository;

    @Autowired
    private ETParamsDefRepository etParamsRepository;

    @Autowired
    private UsageTemplateRepository usageTemplateRepository;

    // 使用缓存
    static HashMap<Integer, ETNodeDTO> etCache = new HashMap<>();

    static HashMap<Integer, List<ETParamDTO>> paramsCache = new HashMap<>();

    @PostConstruct
    public void registerET() {
        initializeETCache();
    }

    public List<ETNodeDTO> getAllET() {
        if (etCache.isEmpty()) initializeETCache();

        List<ETNodeDTO> etList = new ArrayList<>(etCache.values());
        etList.sort(Comparator.comparingInt(ETNodeDTO::getId));
        return etList;
    }

    public Boolean etIsEnabled(Integer id){
        return etCache.containsKey(id);
    }

    public ETNodeDTO getETById(Integer id) {
        return etCache.get(id);
    }

    public String getETUsage(Integer id) {
        ETNodeDTO nodeDTO = etCache.get(id);
        if (nodeDTO == null) {
            throw new ByzerException("et not found");
        }
        return nodeDTO.getUsageTemplate();
    }

    public Map<String, List<String>> getETKeyParams(Integer ETId){
        List<ETParamDTO> etParams = loadETParams(ETId);
        Map<String, List<String>> keyParamNames = new HashMap<>();
        for (ETParamDTO etParamDTO: etParams){
            if (! etParamDTO.getType().equalsIgnoreCase("key")) break;
            String type = etParamDTO.getType().split("/")[0];
            keyParamNames.putIfAbsent(type, Lists.newArrayList());
            keyParamNames.get(type).add(etParamDTO.getName());
        }
        return keyParamNames;
    }

    public String getETName(Integer ETId) {
        ETNodeDTO etNodeDTO = etCache.get(ETId);
        if (etNodeDTO == null) {
            throw new ByzerException("et not found");
        }
        return etNodeDTO.getName();
    }

    public List<ETParamDTO.ValueBehavior> dynamicDepends(DynamicDependsReq dependsReq){
        String valueString = dependsReq.getDependenceValues().stream().map(
                p -> String.format("set %1$s=\"%2$s\"", p.getName(), p.getValue())
        ).collect(Collectors.joining(";\n"));
        List<ETParamDTO.ValueBehavior> valueBehaviors = new ArrayList<>();
        dependsReq.getScripts().forEach(
                p -> {
                    List<ParamValueMap> pList = dynamicQuery(valueString, p.getSql());
                    if (!Objects.isNull(pList)) valueBehaviors.add(ETParamDTO.ValueBehavior.valueOf(p.getName(), pList));
                }
        );
        return valueBehaviors;
    }

    public List<ETParamDTO> loadETParams(Integer ETId) {
        if (paramsCache.containsKey(ETId)) {
            return paramsCache.get(ETId);
        }

        ETNodeDTO etNodeDTO = etCache.get(ETId);
        if (etNodeDTO == null) {
            throw new ByzerException("et not found");
        }
        String ETName = etNodeDTO.getName();

        String sql = "load modelParams.`" + ETName + "` as output;";
        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        List<ETParamResp> engineParams = JacksonUtils.readJsonArray(responseBody, ETParamResp.class);
        //init
        List<ETParamDTO> engineParamDefS = engineParams.stream()
                .map(p -> ETParamDTO.valueOf(p.getParamDef())).collect(Collectors.toList());

        // pretreatment
        List<ETParamsDef> localParams = getEtParams(etNodeDTO);
        Map<String, ETParamsDef> localParamMap = localParams.stream()
                .collect(Collectors.toMap(ETParamsDef::getName, ETParamsDef -> ETParamsDef));

        Map<String, ETParamDTO> result = new LinkedHashMap<>();
        localParams.stream().filter(p -> p.getType().equalsIgnoreCase("Key"))
                .forEach(p-> result.put(p.getName(), ETParamDTO.valueOf(p)));

        for (ETParamDTO engineParam : engineParamDefS) {
            ETParamsDef localParam = localParamMap.get(engineParam.getName());
            if (localParam != null) {
                coverParam(localParam, engineParam);
            } else {
                engineParam.setType("Normal");
            }
            if (result.containsKey(engineParam.getName())){
                ETParamDTO original = result.get(engineParam.getName());
                ETParamUtils.merge(engineParam, original);
            }
            result.put(engineParam.getName(), engineParam);
        }

        paramsCache.put(ETId, new ArrayList<>(result.values()));
        return paramsCache.get(ETId);
    }

    protected List<ETParamsDef> getEtParams(ETNodeDTO etNodeDTO) {
        List<ETParamsDef> localParams = etParamsRepository.findAllByEtId(etNodeDTO.getId());

        UsageTemplate usageTemplate = usageTemplateRepository.findByUsage(etNodeDTO.getEtUsage());
        List<ETParamsDef> templateParams = etParamsRepository.findAllByEtId(usageTemplate.getId());
        if (!Objects.isNull(templateParams)){
            templateParams.addAll(localParams);
            localParams = templateParams;
        }

        // todo
        // other condition:add params
        return localParams;
    }

    protected ETParamDTO coverParam(ETParamsDef localParam, ETParamDTO engineParam) {
        String constraintStr = localParam.getConstraint();
        if (constraintStr != null) {
            ParamConstraint constraint = JacksonUtils.readJson(constraintStr, ParamConstraint.class);
            engineParam.setMaxLength(constraint.getMaxLength());
            engineParam.setMax(constraint.getMax());
            engineParam.setMin(constraint.getMin());
        }
        engineParam.setType(localParam.getType());
        engineParam.setLabel(localParam.getLabel());
        engineParam.setValueType(localParam.getValueType());
        engineParam.setOptional(localParam.getOptional().toString());
        if (Objects.nonNull(localParam.getDefaultValue()) && !localParam.getDefaultValue().isEmpty()){
            engineParam.setDefaultValue(localParam.getDefaultValue());
        }

        String enumValues = localParam.getEnumValues();
        if (enumValues == null || enumValues.isEmpty()) {
            engineParam.setEnumValues(new ArrayList<>());
        } else {
            engineParam.setEnumValues(Arrays.asList(localParam.getEnumValues().split(",")));
        }

        return engineParam;
    }

    private void initializeETCache() {
        if (!engineService.isReady(engineService.getExecutionEngine())) {
            log.error("Unable to reach Byzer-lang engine, skip initializing ET information.");
            return;
        }

        String sql = "!show et;";
        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        log.info("Get ET info from Engine");
        List<RegisterETDTO> registerETDTOS = JacksonUtils.readJsonArray(responseBody, RegisterETDTO.class);

        Map<String, RegisterETDTO> registerMap;
        registerMap = registerETDTOS.stream().collect(Collectors.toMap(RegisterETDTO::getName, RegisterETDTO -> RegisterETDTO));

        log.info("Getting Register ET Info from Metadata...");
        List<RegisterET> ETs = etRepository.findAll();
        // TODO define et covering behavior
        for (RegisterET et : ETs) {
            if (registerMap.containsKey(et.getName()) && et.getEnable()){
                log.info("Loading ET:[" + et.getName() + "] ...");
                RegisterETDTO registerETDTO = registerMap.get(et.getName());

                ETNodeDTO etNodeDTO = ETNodeDTO.valueOf(et);
                if (Objects.isNull(etNodeDTO.getDescription()) || etNodeDTO.getDescription().isEmpty()){
                    etNodeDTO.setDescription(registerETDTO.getDoc());
                }
                UsageTemplate usageTemplate = usageTemplateRepository.findByUsage(etNodeDTO.getEtUsage());
                if (usageTemplate != null) {
                    String template = usageTemplate.getTemplate();
                    String realUsage = StringUtils.replace(template, "$ET_NAME", etNodeDTO.getName());
                    etNodeDTO.setUsageTemplate(realUsage);
                } else {
                    log.error("can not find usage template:{}", etNodeDTO.getEtUsage());
                }
                etCache.put(et.getId(), etNodeDTO);
            }
        }
    }

    private List<ParamValueMap> dynamicQuery(String valueString, String script){
        script = valueString + ";\n" + script;
        String responseBody = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(script)
                        .withAsync("false"));
        List<Map> behaves = JacksonUtils.readJsonArray(responseBody, Map.class);
        if (Objects.isNull(behaves) || behaves.isEmpty()) return null;
        List<ParamValueMap> pList = new ArrayList<>();
        behaves.get(0).forEach(
                (k, v) -> pList.add(ParamValueMap.valueOf(k.toString(), v.toString()))
        );
        return pList;
    }
}
