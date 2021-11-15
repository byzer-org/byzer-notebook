package io.kyligence.notebook.console.support;

public class ParamEnums {

    public enum NodeType {
        ET,
        TRAIN,
        SELECT,
        LOAD,
        SAVE,
        UPDATE,
        PREDICT,
        REGISTER,
    }

    public enum DatasourceType {
        JDBC,
        HIVE,
        DELTALAKE,
        HDFS
    }

    public enum HDFSDataType {
        TEXT,
        JSON,
        CSV,
        PARQUET
    }

    public enum NodeSaveModeEnum {
        OVERWRITE,
        APPEND
    }
}
