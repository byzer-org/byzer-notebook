package io.kyligence.notebook.console.support;

public class ETEnum {

    public enum ParamType {

        KEY("Key"),
        NORMAL("Normal"),
        GROUP_A("Group/A"),
        GROUP_B("Group/B");

        private final String value;
        ParamType(String value) {
            this.value = value;
        }
        public String get() {
            return this.value;
        }
    }

    public enum ParamValueType {

        STRING("STRING"),
        INT("INT"),
        FLOAT("FLOAT"),
        BOOLEAN("BOOLEAN"),
        ENUM("ENUM"),
        MULTI_ENUM("MULTI_ENUM"),
        ARRAY("ARRAY"),
        INPUT("INPUT"),
        INPUT_MODEL("INPUT/MODEL"),
        INPUT_TABLE("INPUT/TABLE"),
        OUTPUT_MODEL_NAME("OUTPUT/MODEL_NAME"),
        OUTPUT_MODEL_PATH("OUTPUT/MODEL_PATH");

        private final String value;
        ParamValueType(String value) {
            this.value = value;
        }
        public String get() {
            return this.value;
        }
    }

    public enum UsageTemplate {

        TRAIN("train", -1);

        private final String name;
        private final Integer etId;

        UsageTemplate(String name, Integer  etId) {
            this.name = name;
            this.etId = etId;
        }
        public String getName() {
            return this.name;
        }
        public Integer getEtId() {
            return this.etId;
        }
    }
}
