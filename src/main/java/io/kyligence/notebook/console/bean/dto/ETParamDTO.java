package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import io.kyligence.notebook.console.bean.entity.ETParamsDef;
import io.kyligence.notebook.console.bean.model.ETParam;
import io.kyligence.notebook.console.bean.model.ParamConstraint;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import io.kyligence.notebook.console.util.ETParamUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class ETParamDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;

    @JsonProperty("type")
    private String type;

    @JsonProperty("value_type")
    private String valueType;

    @JsonProperty("default_value")
    private String defaultValue;

    @JsonProperty("optional")
    private String optional;

    @JsonProperty("enum_values")
    private List<String> enumValues;

    @JsonProperty("required")
    private String required;

    @JsonProperty("derived_type")
    private String derivedType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("depends")
    private List<String> depends;

    @JsonProperty("behave")
    private List<StaticBehave> behave;

    @Data
    @NoArgsConstructor
    public static class StaticBehave {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private List<ValueBehavior> value;

        @JsonProperty("dependency_type")
        private String dependencyType;

        public static StaticBehave valueOf(ETParam.StaticDepends depends) {
            if (depends == null) return null;
            StaticBehave behave = new StaticBehave();
            behave.setName(depends.getName());
            behave.setDependencyType(depends.getDependencyType());
            if (depends.getValue() != null) {
                behave.setValue(depends.getValue().stream().map(ValueBehavior::valueOf).collect(Collectors.toList()));
            }
            return behave;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ValueBehavior {
        @JsonProperty("name")
        private String name;

        @JsonProperty("override")
        private List<ParamValueMap> override;

        public static ValueBehavior valueOf(ETParam.ValueBehave valueBehave) {
            ValueBehavior behavior = new ValueBehavior();
            behavior.setName(valueBehave.getName());
            behavior.setOverride(valueBehave.getOverride().stream().map(
                    o -> ParamValueMap.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, o.getName()),
                            o.getValue()
                    )
            ).collect(Collectors.toList()));
            return behavior;
        }

        public static ValueBehavior valueOf(String name,  List<ParamValueMap> pList) {
            ValueBehavior behavior = new ValueBehavior();
            behavior.setName(name);
            behavior.setOverride(pList);
            return behavior;
        }
    }

    @JsonProperty("dynamic_behave")
    private String dynamicBehave;

    @JsonProperty("max")
    private Double max;

    @JsonProperty("max_length")
    private Integer maxLength;

    @JsonProperty("min")
    private Double min;

    @JsonProperty("array_value_type")
    private ArrayValueType arrayValueType;

    @NoArgsConstructor
    @Data
    public static class ArrayValueType {
        @JsonProperty("value_type")
        private String valueType;

        @JsonProperty("max")
        private String max;

        @JsonProperty("max_length")
        private String maxLength;

        @JsonProperty("min")
        private String min;
    }

    public static ETParamDTO valueOf(ETParam etParam) {
        if (etParam == null) {
            return null;
        }
        ETParamDTO paramDTO = new ETParamDTO();
        paramDTO.arrayValueType = new ArrayValueType();
        paramDTO.name = etParam.getName();
        paramDTO.depends = etParam.getDepends();
        if (etParam.getExtra() != null) {
            paramDTO.description = etParam.getExtra().getDoc();
            if (etParam.getExtra().getOptions() != null) {
                paramDTO.required = etParam.getExtra().getOptions().getRequired();

                String defaultValue = etParam.getExtra().getOptions().getDefaultValue();
                paramDTO.defaultValue = defaultValue.equals("undefined") ? "" : defaultValue;

                paramDTO.derivedType = etParam.getExtra().getOptions().getDerivedType();
                if (Objects.equals(paramDTO.derivedType, ETParamUtils.DerivedType.DYNAMIC_BIND)) {
                    paramDTO.dynamicBehave = etParam.getValueProvider().toString();
                } else if (Objects.equals(paramDTO.derivedType, ETParamUtils.DerivedType.STATIC_BIND)) {
                    paramDTO.behave = etParam.getExtra().getOptions().getDepends().stream().map(StaticBehave::valueOf).collect(Collectors.toList());
                }
            }
        }

        String valueType = etParam.getValueType();
        if (valueType.contains("ARRAY")){
            paramDTO.valueType = "STRING";
        } else {
            paramDTO.valueType = valueType;
        }
        paramDTO.enumValues = etParam.getEnumValues();
        return paramDTO;
    }

    public static ETParamDTO valueOf(ETParamsDef etParamsDef) {
        ETParamDTO paramDTO = new ETParamDTO();
        paramDTO.arrayValueType = new ArrayValueType();
        paramDTO.name = etParamsDef.getName();
        paramDTO.description = etParamsDef.getDescription();
        paramDTO.type = etParamsDef.getType();
        paramDTO.required = etParamsDef.getRequired().toString();
        paramDTO.defaultValue = etParamsDef.getDefaultValue();
        paramDTO.valueType = etParamsDef.getValueType();
        paramDTO.optional = etParamsDef.getOptional().toString();
        paramDTO.derivedType = ETParamUtils.DerivedType.NONE;
        paramDTO.setLabel(etParamsDef.getLabel());
        if (!etParamsDef.getDepends().isEmpty()) {
            paramDTO.depends = JacksonUtils.readJsonArray(etParamsDef.getDepends(), String.class);
        }

        String enumValueStr = etParamsDef.getEnumValues();
        if (enumValueStr == null || enumValueStr.isEmpty()) {
            paramDTO.enumValues = new ArrayList<>();
        } else {
            paramDTO.enumValues = Arrays.asList(etParamsDef.getEnumValues().split(","));
        }

        String constraintStr = etParamsDef.getConstraint();
        if (constraintStr != null) {
            ParamConstraint constraint = JacksonUtils.readJson(constraintStr, ParamConstraint.class);
            paramDTO.setMaxLength(constraint.getMaxLength());
            paramDTO.setMax(constraint.getMax());
            paramDTO.setMin(constraint.getMin());
        }
        return paramDTO;
    }
}
