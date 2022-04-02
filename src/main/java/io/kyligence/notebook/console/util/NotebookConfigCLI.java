package io.kyligence.notebook.console.util;

import com.google.common.collect.Maps;
import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.support.EncryptUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class NotebookConfigCLI {
    public static void main(String[] args) {
        execute(args);
        Unsafe.systemExit(0);
    }

    public static void execute(String[] args) {
        boolean needDec = false;
        if (args.length != 1) {
            if (args.length < 2 || !Objects.equals(EncryptUtils.DEC_FLAG, args[1])) {
                System.out.println("Usage: NotebookConfigCLI conf_name");
                System.out.println("Example: NotebookConfigCLI kylin.server.mode");
                Unsafe.systemExit(1);
            } else {
                needDec = true;
            }
        }

        Properties config = NotebookConfig.getInstance().getProperties();

        BackwardCompatibilityConfig bcc = new BackwardCompatibilityConfig();
        String key = bcc.check(args[0].trim());
        if (!key.endsWith(".")) {
            String value = config.getProperty(key);
            if (value == null) {
                value = "";
            }
            if (needDec && EncryptUtils.isEncrypted(value)) {
                System.out.println(EncryptUtils.decryptPassInNotebook(value));
            } else {
                System.out.println(value.trim());
            }
        } else {
            Map<String, String> props = getPropertiesByPrefix(config, key);
            for (Map.Entry<String, String> prop : props.entrySet()) {
                System.out.println(prop.getKey() + "=" + prop.getValue().trim());
            }
        }
    }

    private static Map<String, String> getPropertiesByPrefix(Properties props, String prefix) {
        Map<String, String> result = Maps.newLinkedHashMap();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String entryKey = (String) entry.getKey();
            if (entryKey.startsWith(prefix)) {
                result.put(entryKey.substring(prefix.length()), (String) entry.getValue());
            }
        }
        return result;
    }
}
