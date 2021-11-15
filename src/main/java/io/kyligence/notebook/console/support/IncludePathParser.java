package io.kyligence.notebook.console.support;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IncludePathParser {

    // pattern like project.io.kyligence.notebook.AAA#1
    public static IncludePath parse(String path) {
        if (path == null) {
            return null;
        }

        // parse project
        if (path.indexOf('.') == -1) {
            log.warn("project can not be null");
            return null;
        }

        IncludePath includePath = new IncludePath();
        includePath.project = path.substring(0, path.indexOf('.'));
        String rest = path.substring(path.indexOf('.') + 1);

        // parse cellId
        if (rest.indexOf('#') != -1) {
            try {
                includePath.cellId = Integer.valueOf(rest.substring(rest.indexOf('#') + 1));
                rest = rest.substring(0, rest.indexOf('#'));
            } catch (NumberFormatException nfe) {
                log.warn("can not parse cellId");
                return null;
            }
        }

        // parse folder
        if (rest.indexOf('.') != -1) {
            includePath.folder = rest.substring(0, rest.lastIndexOf('.'));
            rest = rest.substring(rest.lastIndexOf('.') + 1);
        }

        includePath.notebook = rest;
        return includePath;
    }

    @Data
    @NoArgsConstructor
    public static class IncludePath {

        private String project;

        private String folder;

        private String notebook;

        private Integer cellId;

    }

    public static void main(String[] args) {
        String path = "project.io.kyligence.sdds.aaa#1";
        System.out.println(parse(path));
    }


}
