package io.kyligence.notebook.console.util;

public class EntityUtils {

    public static String toStr(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}
