package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.bean.dto.ParamDefDTO;
import org.apache.commons.compress.utils.Lists;

import java.util.*;

public class TrainNodeSQLRender {
    private static final int maxGroupNum = 9;

    public static class Param {
        public String name;
        public List<String> valueList;
        public String value;
        public Integer listSize;

        public Param(String paramName, List<String> valueList) {
            this.name = paramName;
            this.valueList = valueList;
            this.listSize = valueList.size();
        }

        public Param(String paramName, String value) {
            this.name = paramName;
            this.value = value;
        }

        public String render() {
            return String.format(
                    "%1$s = \"%2$s\"\n",
                    name,
                    value

            );
        }

        public String render(Integer groupId) {
            return String.format(
                    "`fitParam.%1$s.%2$s`=\"%3$s\"\n",
                    groupId,
                    name,
                    value

            );
        }

    }

    private final List<Param> singleParams = Lists.newArrayList();

    //  list group params
    private final List<Param> listGroupParams = Lists.newArrayList();

    //  group param
    private final List<Param> groupParams = Lists.newArrayList();
    private int groupNum = 1;

    public TrainNodeSQLRender(List<ParamDefDTO> paramDef, Map paramMap) {
        paramDef.forEach(d -> {
            if (paramMap.get(d.getName()) == null) return;
            if (ParamDefDTO.ParamTypeEnum.valueOf(d.getValueType()) == ParamDefDTO.ParamTypeEnum.ARRAY){
                addParam(d, (List<String>) paramMap.get(d.getName()));
            } else{
                addParam(d, (String) paramMap.get(d.getName()));
            }
        });
    }

    private void addParam(ParamDefDTO paramDef, List<String> paramValue) {
        if (paramValue.isEmpty()) return;
        Param p = new Param(paramDef.getName(), paramValue);
        listGroupParams.add(p);
        groupNum = Math.min(groupNum * p.listSize, maxGroupNum);
    }

    private void addParam(ParamDefDTO paramDef, String paramValue){
        if (paramValue.isEmpty()) return;
        Param p = new Param(paramDef.getName(), paramValue);
        if (paramDef.getIsGroupParam() != null && paramDef.getIsGroupParam()) {
            groupParams.add(p);
        } else {
            singleParams.add(p);
        }
    }

    /**
     * Backtrack for combination listGroupParams
     *
     * [
     *   Param(name="x", valueList=["1", "2", "3"]),
     *   Param(name="y", valueList=["a", "b"]),
     *   Param(name="z", valueList=["0"])
     * ]
     *
     * return
     *
     * [
     *          [Param(name="x", value="1"), Param(name="y", value="a"), Param(name="z", value="0")],
     *          [Param(name="x", value="2"), Param(name="y", value="a"), Param(name="z", value="0")],
     *          [Param(name="x", value="3"), Param(name="y", value="a"), Param(name="z", value="0")],
     *          [Param(name="x", value="1"), Param(name="y", value="b"), Param(name="z", value="0")],
     *          [Param(name="x", value="2"), Param(name="y", value="b"), Param(name="z", value="0")],
     *          [Param(name="x", value="3"), Param(name="y", value="b"), Param(name="z", value="0")],
     *]
     */
    private void combination(Integer start, Integer size, List<Param> output, Deque<List<Param>> res) {
        if (res.size() == groupNum) return;
        if (start >= size) {
            res.add(new ArrayList<>(output));
            return;
        }
        Param p = listGroupParams.get(start);
        p.valueList.forEach(
                v -> {
                    Param newP = new Param(p.name, v);
                    output.add(newP);
                    combination(start + 1, size, output, res);
                    output.remove(newP);
                }
        );
    }

    public String render(String input, String output) {
        Deque<List<Param>> listParamCombination = new LinkedList<>();
        int listParamNum = listGroupParams.size();

        combination(0, listParamNum, Lists.newArrayList(), listParamCombination);

        String head = String.format(
                "train %1$s as %2$s where\n",
                input,
                output
        );
        List<String> paramStringList = Lists.newArrayList();

        singleParams.forEach(p -> paramStringList.add(p.render()));
        for (int groupId = 0; groupId < groupNum; groupId++) {
            int finalGroupId = groupId;
            groupParams.forEach(p -> paramStringList.add(p.render(finalGroupId)));
            if (listParamNum != 0) {
                listParamCombination.pollFirst().forEach(
                        p -> paramStringList.add(p.render(finalGroupId))
                );
            }

        }
        return head + String.join("and ", paramStringList) + ";";
    }
}

