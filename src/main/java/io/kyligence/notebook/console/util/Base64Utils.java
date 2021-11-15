package io.kyligence.notebook.console.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class Base64Utils {

    public static String[] getUserAndPwd(String basicAuth) {
        String encodedTxt = StringUtils.substringAfter(basicAuth, "Basic ");
        String decodeUserPwd = new String(Base64.decodeBase64(encodedTxt), StandardCharsets.UTF_8);

        String user = StringUtils.substringBefore(decodeUserPwd, ":");
        String pwd = StringUtils.substringAfter(decodeUserPwd, ":");
        return new String[]{user, pwd};
    }

}
