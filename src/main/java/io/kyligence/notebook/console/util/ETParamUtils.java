package io.kyligence.notebook.console.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.kyligence.notebook.console.bean.dto.ETParamDTO;
import io.kyligence.notebook.console.bean.model.ETParam;
import io.kyligence.notebook.console.bean.model.ETParam;
import io.kyligence.notebook.console.bean.model.ETParamResp;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import io.kyligence.notebook.console.support.ETEnum;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ETParamUtils {

    public interface DerivedType {
        String DYNAMIC_BIND = "DYNAMIC_BIND";
        String STATIC_BIND = "STATIC_BIND";
        String NONE = "NONE";
    }

    public interface DependencyType {
        String VALUE_BIND = "VALUE_BIND";
        String NOT_EMPTY_BIND = "NOT_EMPTY_BIND";

    }


    final static private Pattern tagBindPattern = Pattern.compile("\\[tag__(.+?)__(.+?)]|\\[tag__(.+?)]");

    public static String typeConvert(String type) {
        switch (type.toLowerCase()) {
            case "int":
            case "long":
                return ETEnum.ParamValueType.INT.get();
            case "float":
            case "double":
                return ETEnum.ParamValueType.FLOAT.get();
            case "boolean":
                return ETEnum.ParamValueType.BOOLEAN.get();
            default:
                return ETEnum.ParamValueType.STRING.get();
        }
    }

    public static void typeHandle(ETParam etParam) {
        String type = typeConvert(etParam.getExtra().getOptions().getValueType());
        etParam.setValueType(type);
        if (etParam.getValues() != null) {
            if (etParam.getType().equalsIgnoreCase("Select") || etParam.getSubType().equalsIgnoreCase("Select")) {
                etParam.setValueType(ETEnum.ParamValueType.ENUM.get());
                etParam.setEnumValues(
                        etParam.getValues().stream().map(ParamValueMap::getValue).collect(Collectors.toList())
                );
            } else if (etParam.getType().equalsIgnoreCase("CheckBox")|| etParam.getSubType().equalsIgnoreCase("CheckBox")) {
                etParam.setValueType(ETEnum.ParamValueType.MULTI_ENUM.get());
                etParam.setEnumValues(
                        etParam.getValues().stream().map(ParamValueMap::getValue).collect(Collectors.toList())
                );
            }
        }
    }

    public static void checkStaticBindParam(ETParam etParam) {
        Map<String, ETParam.StaticDepends> uniqueDepends = Maps.newHashMap();
        etParam.getExtra().getOptions().setDepends(etParam.getDepends().stream().map(
                name -> dependsParse(name, etParam, uniqueDepends)
        ).collect(Collectors.toList()));

    }

    public static ETParam.StaticDepends dependsParse(String name, ETParam etParam, Map<String, ETParam.StaticDepends> uniqueDepends) {
        ETParam.ValueBehave behave = new ETParam.ValueBehave();
        String dependType;
        String dependName;
        if (name.startsWith(":")) {
            dependType = DependencyType.VALUE_BIND;
            String[] nameAndValue = name.substring(1).split("==");
            dependName = nameAndValue[0];
            behave.setName(nameAndValue[1]);
        } else {
            dependType = DependencyType.NOT_EMPTY_BIND;
            dependName = name;
        }
        behave.setOverride(
                Lists.newArrayList(
                        ParamValueMap.valueOf("required", etParam.getExtra().getOptions().getRequired()),
                        ParamValueMap.valueOf("enabled", "true")
                )
        );
        if (!uniqueDepends.containsKey(dependName)) {
            ETParam.StaticDepends depends = new ETParam.StaticDepends();
            depends.setDependencyType(dependType);
            depends.setName(name);
            depends.setValue(Lists.newArrayList());
        }
        ETParam.StaticDepends depends = uniqueDepends.get(dependName);
        depends.getValue().add(behave);
        return depends;
    }

    public static void checkTagBindParam(ETParamResp etParamResp, ETParam etParam) {
        String name = etParamResp.getParam();
        Matcher matched = tagBindPattern.matcher(name);
        if (!matched.find()) return;
        name = name.split("\\.")[name.split("\\.").length - 1];
        ETParam.StaticDepends depends = new ETParam.StaticDepends();
        ETParam.ValueBehave behave = new ETParam.ValueBehave();

        if (matched.group(3) != null) {
            depends.setDependencyType(DependencyType.NOT_EMPTY_BIND);
            depends.setName(matched.group(3));
        } else {
            depends.setName(matched.group(1));
            behave.setName(matched.group(2));
            depends.setDependencyType(DependencyType.VALUE_BIND);
        }
        behave.setOverride(Lists.newArrayList(
                ParamValueMap.valueOf("required", etParam.getExtra().getOptions().getRequired()),
                ParamValueMap.valueOf("enabled", "true")
        ));
        depends.setValue(Lists.newArrayList(behave));
        etParam.setName(name);
        etParam.getExtra().getOptions().setDerivedType(DerivedType.STATIC_BIND);
        etParam.getExtra().getOptions().setDepends(Lists.newArrayList(depends));
    }


    public static ETParam handle(ETParamResp etParamResp, ETParam etParam) {
        if (!Objects.isNull(etParam.getValueProvider()) && etParam.getType().equalsIgnoreCase("Dynamic")) {
            etParam.getExtra().getOptions().setDerivedType(DerivedType.DYNAMIC_BIND);
        }
        if (etParam.getExtra() != null && etParam.getExtra().getOptions() != null) {
            if (Objects.equals(etParam.getExtra().getOptions().getDerivedType(), DerivedType.STATIC_BIND)) {
                checkStaticBindParam(etParam);
            } else {
                checkTagBindParam(etParamResp, etParam);
            }
            typeHandle(etParam);
        }
        return etParam;
    }


    public static void merge(ETParamDTO engineParam, ETParamDTO original) {
        if (Objects.isNull(engineParam.getBehave())) engineParam.setBehave(new ArrayList<>());
        if (Objects.isNull(engineParam.getDepends())) engineParam.setDepends(new ArrayList<>());

        if (!Objects.isNull(original.getBehave())) engineParam.getBehave().addAll(original.getBehave());
        if (!Objects.isNull(original.getDepends())) engineParam.getDepends().addAll(original.getDepends());
        engineParam.setBehave(mergeBehave(engineParam.getBehave()));
    }

    public static List<ETParamDTO.StaticBehave> mergeBehave(List<ETParamDTO.StaticBehave> behaves) {
        Map<String, ETParamDTO.StaticBehave> behaveMap = new HashMap<>();
        for (ETParamDTO.StaticBehave b : behaves) {
            if (behaveMap.containsKey(b.getName())) {
                ETParamDTO.StaticBehave cB = behaveMap.get(b.getName());
                cB.getValue().addAll(b.getValue());
            } else {
                behaveMap.put(b.getName(), b);
            }

        }
        return new ArrayList<>(behaveMap.values());
    }

}
