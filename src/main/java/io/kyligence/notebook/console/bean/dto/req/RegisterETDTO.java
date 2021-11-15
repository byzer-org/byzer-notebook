package io.kyligence.notebook.console.bean.dto.req;

import lombok.Data;

@Data
public class RegisterETDTO {
    private String name;
    private String algType;
    private String sparkCompatibility;
    private String doc;
    private String docType;
}
