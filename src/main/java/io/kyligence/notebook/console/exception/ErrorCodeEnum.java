package io.kyligence.notebook.console.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodeEnum {
    /**
     * Global exception
     **/
    UNKNOWN_ERROR("KN-1000001", "Unknown Error", ""),

    METHOD_ARG_NOT_VALID("KN-1000002", "Method Argument Not Valid", ""),

    ACCESS_DENIED("KN-1000003", "Access Denied", ""),

    DATA_ACCESS_FAILED("KN-1000004", "Data Access Failed", ""),

    ENGINE_ACCESS_FAILED("KN-1000005", "Engine Access Failed", ""),

    NO_SUCH_TYPE("KN-1000006", "No Such Type", ""),

    INCORRECT_NAME("KN-1000007", "Incorrect Name", "The {} name can only contain numbers, letters, Chinese characters, and underscores"),

    /**
     * Notebook exception
     **/
    DUPLICATE_NOTEBOOK_NAME("KN-1010001", "Duplicate Notebook Name", ""),

    NOTEBOOK_NOT_EXIST("KN-1010002", "Notebook Not Exist", ""),

    CELL_NOT_EXIST("KN-1010003", "Cell Not Exist", ""),

    CELL_NOT_FOUND("KN-1010004", "Cell Not Found", "Can not find cell {} in notebook."),

    CELL_CAN_NOT_DELETE("KN-1010005", "Cell Can Not Delete", "Can not delete last cell in notebook."),

    NOTEBOOK_NOT_AVAILABLE("KN-1010006", "Notebook Not Available", ""),

    NOTEBOOK_NUM_REACH_LIMIT("KN-1010007", "Can Not Create More Notebooks", "{}"),

    /**
     * Workflow exception
     **/
    DUPLICATE_WORKFLOW_NAME("KN-1011001", "Duplicate Workflow Name", ""),

    NODE_NOT_EXIST("KN-1011002", "Node Not Exist", ""),

    WORKFLOW_NOT_EXIST("KN-1011003", "Workflow Not Exist", ""),

    WORKFLOW_NOT_AVAILABLE("KN-1011004", "Workflow Not Available", ""),

    WORKFLOW_HAS_CYCLE("KN-1011005", "Workflow Has Cycle", ""),

    UNSUPPORTED_NODE_TYPE("KN-1011006", "Unsupported Node Type", ""),

    UNSUPPORTED_DATASOURCE("KN-1011007", "Unsupported Datasource", ""),

    MULTIPLE_SQL_INPUT("KN-1011008", "Multiple Sql Input", "Multiple sql input is forbidden, please check your input."),

    PWD_DECODE_ERROR("KN-1011009", "Password Decode Hex Error", ""),

    PWD_DECRYPTION_ERROR("KN-1011010", "Password Decryption Error", ""),

    NODE_OUTPUT_ALREADY_EXIST("KN-1011011", "Node Output Already Exist", "Output {} already exist in this workflow, please rename it."),

    SELECT_SQL_SYNTAX_ERROR("KN-1011012", "Select Sql Syntax Error", ""),

    WORKFLOW_NUM_REACH_LIMIT("KN-1011013", "Can Not Create More Workflows", "{}"),

    CAN_NOT_CHANGE_NODE_TYPE("KN-1011014", "Can't Change Type of Exist Node",""),

    /**
     * Engine exception
     **/
    MLSQL_EXCEPTION("KN-1012001", "Mlsql Exception", "{}"),

    ENGINE_ERROR("KN-1012002", "Mlsql Execute Error", "{}"),

    ENGINE_ACCESS_EXCEPTION("KN-1012002", "Connection Refused", ""),

    /**
     * File exception
     **/
    FILE_ALREADY_EXIST("KN-1013001", "File Already Exist", ""),

    UNSUPPORTED_EXT_NAME("KN-1013002", "Unsupported Extension Name", "Unsupported extension name, only support .mlnb and .mlwf"),

    EMPTY_FILE("KN-1013003", "Empty File", ""),

    FOLDER_NOT_EXIST("KN-1013004", "Folder Not Exist", ""),

    DUPLICATE_FOLDER_NAME("KN-1013005", "Duplicate Folder Name", ""),

    SAME_SUB_FOLDER_NAME("KN-1013006", "Same Sub-folder Name", "Target folder has same sub-folder name."),

    FOLDER_NOT_AVAILABLE("KN-1013007", "Folder Not available", "Can not visit other's folder."),

    FILE_NOT_EXIST("1013008", "File Not Exist", ""),

    UPLOADED_FILE_TOO_LARGE("KN-1013009", "Uploaded File Too Large", "The size of the current file exceeds the upper limit: {} MB"),

    UPLOADED_FILE_REACH_LIMIT("KN-1013010", "Total Uploaded File Size Limit", "The total file size can be uploaded is {} MB"),
    /**
     * Setting exception
     **/

    CONNECTION_NOT_EXIST("KN-1014001", "Connection Not Exist", ""),

    CONNECTION_REFUSED("KN-1014002", "Connect Refused", "Unable to connect the datasource, please check your setting."),

    DUPLICATE_CONNECTION_NAME("KN-1014003", "Duplicate Connection Name", ""),

    CONNECTION_NOT_AVAILABLE("KN-1014004", "Connection Not Available", ""),

    NODE_DEFINE_NOT_EXIST("KN-1014005", "Node Define Not Exist", ""),

    PARAM_DEFINE_NOT_EXIST("KN-1014006", "Parameter Define Not Exist", ""),
    /**
     * Data Catalog exception
     **/

    FILE_FORMAT_MISMATCH("KN-1015001", "File Format Mismatch", "The uploaded data source format does not match."),

    /**
     * Starting exception
     **/

    LOAD_CONFIG_ERROR("KN-1016001", "Load Config Error", "Fail to locate {}."),

    ENV_NOT_FOUND("KN-1016002", "Env variable Not Found", "Didn't find NOTEBOOK_HOME in system property or system env, please set."),

    /**
     * User exception
     **/


    USER_ALREADY_EXIST("KN-1017001", "User Already Exist", ""),

    AUTH_FAILED("KN-1017002", "Access Denied", "Please check your username or password"),

    ACCESS_DISABLED_API("KN-1017003", "Not Found", ""),

    /**
     * Job exception
     **/

    JOB_NOT_EXIST("KN-1018001", "Job Not Exist", "Can not kill job {}, job not exist."),

    ;

    private final String code;
    private final String msg;
    private final String detail;

    private static final Map<String, ErrorCodeEnum> map = new HashMap<>();

    static {
        for (ErrorCodeEnum errorCodeEnum : ErrorCodeEnum.values()) {
            map.put(errorCodeEnum.name(), errorCodeEnum);
        }
    }

    ErrorCodeEnum(String code, String msg, String detail) {
        this.code = code;
        this.msg = msg;
        this.detail = detail;
    }

    public static ErrorCodeEnum getByCode(String code) {
        return map.get(code);
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getDetail() {
        return this.detail;
    }

    public String getReportMsg() {
        return detail.isEmpty() ? msg : msg + " : " + detail;
    }

}
