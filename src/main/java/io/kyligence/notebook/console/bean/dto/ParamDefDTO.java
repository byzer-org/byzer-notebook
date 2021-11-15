package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.NodeDefInfo;
import io.kyligence.notebook.console.bean.entity.ParamDefInfo;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ParamDefDTO {
    public enum ParamTypeEnum {
        STRING,
        INT,
        FLOAT,
        BOOL,
        ENUM,
        ARRAY,
        PARAM_ENUM,
        ELEMENT
    }

    @JsonProperty
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("value_type")
    private String valueType;

    @JsonProperty("default_value")
    private String defaultValue;

    @JsonProperty("required")
    private Boolean required;

    /**
     * Value interval for `INT` and `FLOAT` valueType
     */
    @JsonProperty("value_interval")
    private ValueInterval valueInterval;

    /**
     * Elements for `ENUM` valueType
     */
    @JsonProperty("enum_values")
    private List<String> enumValues;

    /**
     * Elements for `PARAM_ENUM` valueType
     */
    @JsonProperty("enum_params")
    private List<ParamDefDTO> enumParams;

    /**
     * `ELEMENT` have related param
     */
    @JsonProperty("bind_params")
    private List<ParamDefDTO> bindParams;

    /**
     * `ARRAY` Element type
     */
    @JsonProperty("array_value_type")
    private ParamDefDTO arrayValueType;

    /**
     * Max length of `ARRAY`
     */
    @JsonProperty("array_max_size")
    private Integer arrayMaxSize;

    @JsonProperty("is_group_param")
    private Boolean isGroupParam;

    @Data
    @NoArgsConstructor
    public static class ValueInterval {
        @JsonProperty("max")
        private Double max;

        @JsonProperty("min")
        private Double xmin;
    }

    public static ParamDefDTO valueOf(ParamDefInfo paramDefInfo) {
        ParamDefDTO paramDefDTO = new ParamDefDTO();
        paramDefDTO.setId(paramDefInfo.getId());
        paramDefDTO.setName(paramDefInfo.getName());
        paramDefDTO.setDescription(paramDefInfo.getDescription());
        paramDefDTO.setValueType(paramDefInfo.getValueType());
        paramDefDTO.setDefaultValue(paramDefInfo.getDefaultValue());
        paramDefDTO.setRequired(paramDefInfo.getRequired());
        paramDefDTO.setIsGroupParam(paramDefInfo.getIsGroupParam());
        ParamTypeEnum paramType = ParamTypeEnum.valueOf(paramDefDTO.getValueType());
        if (paramDefInfo.getConstrain() != null) {
            String constrain = paramDefInfo.getConstrain();
            switch (paramType) {
                case FLOAT:
                case INT:
                    // constrain is {'max': , 'min': }
                    paramDefDTO.setValueInterval(JacksonUtils.readJson(constrain, ValueInterval.class));
                    break;
                case ARRAY:
                    // constrain is the size limit of array
                    paramDefDTO.setArrayMaxSize(Integer.parseInt(constrain));
                    break;
                case ENUM:
                    // constrain is ['a', 'b']
                    paramDefDTO.setEnumValues(JacksonUtils.readJsonArray(constrain, String.class));
                    break;
            }
        }
        return paramDefDTO;
    }

    public static ParamDefDTO valueOf(ParamDefInfo defInfo, List<ParamDefInfo> paramDefInfoList) {
        ParamDefDTO dto = valueOf(defInfo);
        Map<Integer, List<ParamDefDTO>> bondMap = new HashMap<>();
        paramDefInfoList.forEach(paramDefInfo -> {
            Integer bindTo = paramDefInfo.getBind();
            if (bindTo != null){
                bondMap.putIfAbsent(bindTo, Lists.newArrayList());
                bondMap.get(bindTo).add(ParamDefDTO.valueOf(paramDefInfo));
            }
        });
        return bindParam(dto, bondMap);
    }

    public static ParamDefDTO bindParam(ParamDefDTO paramDefDTO, Map<Integer, List<ParamDefDTO>> bondMap) {
        if (!bondMap.containsKey(paramDefDTO.getId())) {
            return paramDefDTO;
        }
        ParamTypeEnum paramType = ParamTypeEnum.valueOf(paramDefDTO.getValueType());

        switch (paramType) {
            case ARRAY:
                paramDefDTO.setArrayValueType(bondMap.get(paramDefDTO.getId()).get(0));
                break;
            case PARAM_ENUM:
                List<ParamDefDTO> enumParams = bondMap.get(paramDefDTO.getId()).stream().map(
                        dto -> bindParam(dto, bondMap)
                ).collect(Collectors.toList());
                paramDefDTO.setEnumParams(enumParams);
                break;
            case ELEMENT:
                paramDefDTO.setBindParams(bondMap.get(paramDefDTO.getId()));
                break;

        }
        return paramDefDTO;
    }
}
