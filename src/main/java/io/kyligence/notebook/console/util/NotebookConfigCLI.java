/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                System.out.println("Example: NotebookConfigCLI notebook.url");
                Unsafe.systemExit(1);
            } else {
                needDec = true;
            }
        }

        Properties config = NotebookConfig.getInstance().getProperties();

        String key = args[0].trim();
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
