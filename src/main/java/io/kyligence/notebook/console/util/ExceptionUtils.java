package io.kyligence.notebook.console.util;

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
}
