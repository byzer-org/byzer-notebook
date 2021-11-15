package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class Response<T> {

    private String code = "";

    @JsonInclude
    private T data;

    private String msg;

    public Response<T> code(String code) {
        this.code = code;
        return this;
    }

    public Response<T> data(T data) {
        this.data = data;
        return this;
    }

    public Response<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

}
