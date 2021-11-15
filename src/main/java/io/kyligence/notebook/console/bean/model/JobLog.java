package io.kyligence.notebook.console.bean.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JobLog {

    private List<String> value;

    private Integer offset;
}
