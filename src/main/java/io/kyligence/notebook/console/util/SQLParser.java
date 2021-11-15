package io.kyligence.notebook.console.util;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import org.apache.commons.compress.utils.Lists;

import java.util.*;

public class SQLParser {
    public static Map<String, List<String>> parseSQLSelectTable(String sql) {
        String[] sqlList = sql.split("\\s*;");
        if (sqlList.length > 1) {
            throw new ByzerException(ErrorCodeEnum.MULTIPLE_SQL_INPUT);
        }
        Map<String, List<String>> tableNameMap = new HashMap<>();
        List<String> inputTable = Lists.newArrayList();
        List<String> outputTable = Lists.newArrayList();
        tableNameMap.put("input", inputTable);
        tableNameMap.put("output", outputTable);

        if (sqlList.length == 0 || sqlList[0].isEmpty()){
            return tableNameMap;
        }
        String validSQL = sqlList[0];
        String[] chunks = validSQL.split("\\s+");
        if (chunks.length <= 1) {
            throw new ByzerException(ErrorCodeEnum.SELECT_SQL_SYNTAX_ERROR);
        }

        if (chunks[chunks.length - 2].equalsIgnoreCase("as")) {
            String outputTableName = chunks[chunks.length - 1];
            outputTable.add(outputTableName);
            validSQL = validSQL.replaceAll("((?i)as)[\\s|\\n]+" + outputTableName, "");
        }
        SQLStatement statement = SQLUtils.parseSingleMysqlStatement(validSQL);
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        statement.accept(visitor);

        visitor.getTables().keySet().forEach(tbName -> inputTable.add(tbName.getName()));

        return tableNameMap;

    }
}