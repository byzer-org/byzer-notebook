package io.kyligence.notebook.console.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ExceptionUtils {

    public static String getRootCause(Throwable t) {
        Throwable origin = t;
        while (t != null) {
            if (t.getCause() == null) {
                String msg = t.getMessage();
                if (msg == null) {
                    msg = t.toString();
                }
                return msg;
            }
            t = t.getCause();
        }
        if (origin == null) {
            return "";
        }
        return origin.getMessage();
    }

    public static String getRootCause(String ex) {
        if (ex == null || ex.isEmpty()) {
            return "";
        }
        List<String> list = Arrays.asList(ex.split("caused by: \n"));
        List<String> subList = Arrays.asList(list.get(list.size() - 1).split("\n"));
        String regex = (".*([a-zA-Z]*[.][a-zA-Z]*[:][0-9]*\\))");

        StringBuilder rootCause = new StringBuilder();
        for (String term : subList) {
            if (Pattern.matches(regex, term)) break;
            if (rootCause.toString().isEmpty() || rootCause.toString().startsWith(" ")) {
                rootCause.append(term);
            }
        }
        return rootCause.toString();
    }


}
