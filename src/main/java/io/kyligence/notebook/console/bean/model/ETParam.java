package io.kyligence.notebook.console.bean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ETParam {
    @JsonProperty("name")
    private String name;

    @JsonProperty("tpe")
    private String type;

    @JsonProperty("subTpe")
    private String subType;

    @JsonProperty("depends")
    private List<String> depends;

    @JsonProperty("values")
    private List<ParamValueMap> values;

    @JsonProperty("enumValues")
    private List<String> enumValues;

    @JsonProperty("valueType")
    private String valueType;

    @JsonProperty("extra")
    private ExtraInfo extra;

    @JsonProperty("valueProviderName")
    private Object valueProvider;

    @JsonProperty("value")
    private String value;


    @Data
    @NoArgsConstructor
    public static class ExtraInfo {
        @JsonProperty("doc")
        private String doc;

        @JsonProperty("label")
        private String label;

        @JsonProperty("options")
        private Options options;

    }

    @Data
    @NoArgsConstructor
    public static class Options {
        @JsonProperty("valueType")
        private String valueType;

        @JsonProperty("derivedType")
        private String derivedType;

        @JsonProperty("required")
        private String required;

        @JsonProperty("defaultValue")
        private String defaultValue;

        @JsonProperty("currentValue")
        private String currentValue;

        @JsonProperty("depends")
        private List<StaticDepends> depends;
    }

    @Data
    @NoArgsConstructor
    public static class StaticDepends {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private List<ValueBehave> value;

        @JsonProperty("dependencyType")
        private String dependencyType;
    }

    @Data
    @NoArgsConstructor
    public static class ValueBehave {
        @JsonProperty("name")
        private String name;

        @JsonProperty("override")
        private List<ParamValueMap> override;
    }
}

