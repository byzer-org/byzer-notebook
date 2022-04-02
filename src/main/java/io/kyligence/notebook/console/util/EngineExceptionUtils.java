package io.kyligence.notebook.console.util;

import org.springframework.data.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EngineExceptionUtils {

    private EngineExceptionUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern pythonLineRegex = Pattern.compile("^(File \"<string>\", line )(\\d+)(,.*)");

    public static Pair<String, Map<Integer, Integer>> parseCodeTypeAndOffset(String script) {
        String codeType = "byzer";
        Map<Integer, Integer> offsetTracker = new LinkedHashMap<>();
        int offset = 0;
        int lineno = 0;

        boolean prevIsHint = false;
        for (String line : script.split("\n")) {
            lineno++;
            if (line.trim().startsWith("#%")) {
                String header = StringUtils.stripStart(line.trim(), "#%");
                codeType = header.contains("=") ? codeType : header;
                offset++;
                prevIsHint = true;
            } else if (prevIsHint) {
                offsetTracker.put(lineno - offset, offset);
                prevIsHint = false;
            }
        }
        return Pair.of(codeType, offsetTracker);
    }

    public static String parseStackTrace(String script, String stackTrace) {
        if (StringUtils.isBlank(script) || StringUtils.isBlank(stackTrace)) return stackTrace;
        Pair<String, Map<Integer, Integer>> codeTypeAndOffset = parseCodeTypeAndOffset(script);
        if (codeTypeAndOffset.getFirst().equalsIgnoreCase("python")) {
            String result = parsePythonError(stackTrace, codeTypeAndOffset.getSecond());
            return result.isEmpty() ? stackTrace : result;
        }
        return stackTrace;
    }

    public static String parsePythonError(String stackTrace, Map<Integer, Integer> codeOffset) {
        List<String> result = new ArrayList<>();
        boolean start = false;
        for (String line : stackTrace.split("\n")) {
            if (start && line.contains("PythonRunner.scala")) break;

            if (line.trim().startsWith("File")) {
                start = true;
                result.add(pythonLinenoAlign(line.trim(), codeOffset));

            } else if (start) {
                result.add(line);
            }
        }
        return StringUtils.join(result, "\n");
    }

    public static String pythonLinenoAlign(String line, Map<Integer, Integer> codeOffset) {
        Matcher m = pythonLineRegex.matcher(line);
        if (!m.find()) return line;
        Integer lineno = Integer.valueOf(m.group(2));
        int lineMark = 0;
        for (Integer mark : codeOffset.keySet()) {
            if (mark > lineno) break;
            lineMark = mark;
        }
        return m.group(1) + (lineno + codeOffset.get(lineMark)) + m.group(3);
    }

    public static String getRootCause(String ex) {
        if (StringUtils.isBlank(ex)) {
            return "";
        }
        // python stacktrace
        if (ex.trim().startsWith("File")) {
            String[] lines = ex.split("\n");
            return lines[lines.length - 1];
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
