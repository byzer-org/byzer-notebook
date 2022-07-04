package io.kyligence.notebook.console.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LoadRestRewriter {
    public static class SplitResult {
        public boolean splitResult;
        public String[] arr;

        public SplitResult(boolean result, String[] arr) {
            this.splitResult = result;
            this.arr = arr;
        }
    }

    public static final String LOAD = "load";
    public static final String AS = "as";
    public static final String COMMA = ";";
    private static Pattern BYZER_TABLE_NAME_PTN = Pattern.compile("^[A-Za-z]([A-Za-z0-9_]*)");

    /**
     * Add "AND `config.enableRestDataSourceRequestCleaner`="true" to Load Rest.``.
     * If tokens are not start with Load Rest.`, not ends with "as <tableName>;" , returns tokens as it is.
     * For example: Load Rest.`www.google.com` AS output; becomes
     * Load Rest.`www.google.com` AND `enableRestDataSourceRequestCleaner`="true" AS output
     * @param sql
     * @return
     */
    public static String appendConf(String sql) {
        // 1. Remove comments
        final String commentsRemovedSql = removeComments(sql);
        // 2. Split by comma into multiple SQLs
        String[] sqlArr = splitByComma(commentsRemovedSql);
        List<String> newSqlList = new ArrayList<>();
        for (int i = 0; i < sqlArr.length; i ++) {
            // 3. Split load statement.
            SplitResult splitResult = splitByBlankOrLineSeparator(sqlArr[i]);
            if( splitResult.splitResult ) {
                String[] tokens = splitResult.arr;
                // 4. Add config.enableRestDataSourceRequestCleaner`="true" to load Rest
                String[] newTokens = appendConf( removeBlanksLineSeparatorAndComma(tokens) );
                if( newTokens != null && newTokens.length > 0 ) {
                    newSqlList.add( String.join(" ", newTokens));
                }
            }
            else {
                newSqlList.add(sqlArr[i]);
            }
        }
        if( ! newSqlList.isEmpty() && StringUtils.isNotBlank(newSqlList.get(newSqlList.size() -1))
                && ! newSqlList.get(newSqlList.size()-1).endsWith(COMMA)  ) {
            newSqlList.set( newSqlList.size() -1,  newSqlList.get( newSqlList.size() -1 ) + COMMA);
        }
        // 5. Join multiple SQLs by ;\n into one
        return String.join( COMMA + System.lineSeparator(), newSqlList);
    }

    private static String[] splitByComma(String sql) {
        if( StringUtils.isEmpty(sql)) {
            return new String[0];
        }
        String[] arr =  sql.split(COMMA );
        return arr;
    }

    /**
     * example : "abc\ne a" --> ["abc", "e", "a"].
     * @param sql
     * @return
     */
    public static SplitResult splitByBlankOrLineSeparator(String sql) {
        if(StringUtils.isBlank( sql )) {
            return new SplitResult(true, new String[0]);
        }
        List<String> strArr = new ArrayList<>();
        char[] charArr = sql.toCharArray();
        String word = null;

        for (int i = 0; i < charArr.length;  i ++) {
            char c = charArr[i];
            // 32: blank 10: line separator
            if( (char)32 == c || (char)10 == c ) {
                if( word != null ) {
                    strArr.add(word);
                    word = null;
                }
            }
            else {
                word = word == null ? String.valueOf(c) : word + c;
                if( i == charArr.length -1 ) {
                    strArr.add(word);
                }
            }
        }
        // Only process Load
        if( strArr.size() > 0 && LOAD.equalsIgnoreCase( strArr.get(0)) ) {
            String[] arr = new String[strArr.size()];
            return new SplitResult( true, strArr.toArray(arr) );
        }
        else {
            String[] arr = new String[1];
            arr[0] = sql;
            return new SplitResult(false, arr);
        }
    }


    private static String removeComments(String sql) {
        if( StringUtils.isBlank(sql) ) {
            return sql;
        }
        return Arrays.stream( sql.split(System.lineSeparator()) )
                .filter( s -> StringUtils.isNotBlank(s) && ! s.startsWith("--"))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Remove blanks line-separator and trailing comma from load statement.
     * Non load statement is not changed
     * For example: ["a", "", ";", "\n"] --> ["a", "", ";", "\n"]
     *              ["\nload","Rest"]           --> ["load","Rest"]
     * @param tokens
     * @return
     */
    private static String[] removeBlanksLineSeparatorAndComma(String[] tokens) {
        if( tokens == null || tokens.length == 0) {
            return tokens;
        }
        String[] newTokens = new String[ tokens.length ];
        int k = 0;
        for (int i = 0; i < tokens.length && k < tokens.length; i ++) {
            // Ignore blank string and line-breaker (\n in Linux)
            if( ! StringUtils.isBlank( tokens[i] ) && ! System.lineSeparator().equals( tokens[i] )) {
                // Remove leading line separators in token
                char[] tokenArr = tokens[i].toCharArray();
                char[] newTokenArr = new char[tokenArr.length];
                int j1 = 0;
                boolean lineSeparator = true;
                for( int j = 0; j < tokenArr.length && j1 < tokenArr.length ; j ++ ) {
                    if( (char)10 != tokenArr[j] || ! lineSeparator ) {
                        lineSeparator = false;
                        newTokenArr[ j1 ++ ] = tokenArr[j];
                    }
                }
                char[] tmp = new char[j1];
                System.arraycopy( newTokenArr, 0, tmp, 0, j1);
                newTokens[ k ++ ] = String.valueOf(  tmp );
            }
        }
        if( k == 0 ) {
            return new String[0];
        }
        String[] newToken2 = null;
        if( newTokens[ k-1].endsWith( COMMA )) {
            // Remove trailing ;
            if( ";".equals( newTokens[k - 1] )) {
                newToken2 = new String[k -1];
                System.arraycopy(newTokens, 0, newToken2, 0, k-1);
            }
            else {
                newToken2 = new String[k];
                newTokens[k-1] = newTokens[k-1].substring(0, newTokens[k-1].length() -1 );
                System.arraycopy(newTokens, 0, newToken2, 0, k);
            }
        }
        else {
            newToken2 = new String[k];
            System.arraycopy(newTokens, 0, newToken2, 0, k);
        }
        return newToken2;
    }

    /**
     * @param tokens  pre-processed by removeBlanksAndComma, should not ends with ";", starts with load Rest,
     *                ends with as <table_name>
     * @return
     */
    private static String[] appendConf(String[] tokens) {

        final int oldLen = tokens.length;
        if( tokens == null || oldLen < 2 ) {
            return tokens;
        }
        final int len = tokens.length;
        final int last = len - 1;

        // Check if first token is load -- ignore case
        if( ! LOAD.equalsIgnoreCase( tokens[0] ) ) {
            return tokens;
        }
        // Check if the second token startsWith Rest.`
        if( ! tokens[1].startsWith("Rest.") ) {
            return tokens;
        }
        // check if the last 2 token is as <tableName>
        if( ! BYZER_TABLE_NAME_PTN.matcher(tokens[last ]).matches()) {
            return tokens;
        }
        if( ! AS.equalsIgnoreCase( tokens[last - 1 ])) {
            return tokens;
        }
        // Create a new list , copy original tokens with " AND `enableRestDataSourceRequestCleaner`="true" "
        String[] newTokens = new String[len + 2];
        System.arraycopy(tokens, 0, newTokens,0, len - 2 );
        newTokens[len - 2  ] = "AND";
        newTokens[len - 2 + 1 ] = "`config.enableRestDataSourceRequestCleaner`=\"true\"";
        newTokens[len - 2 + 2 ] = tokens[ last - 1 ];
        newTokens[len - 2 + 3 ] = tokens[ last ];
        return newTokens;
    }
}
