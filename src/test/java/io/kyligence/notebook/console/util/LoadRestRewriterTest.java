package io.kyligence.notebook.console.util;

import org.junit.Test;

import static io.kyligence.notebook.console.util.LoadRestRewriter.splitByBlankOrLineSeparator;
import static org.junit.Assert.*;

public class LoadRestRewriterTest {
    @Test
    public void simple() {
        String sql = "load Rest.`http://www.163.com` AS otu;";
        String newSql = LoadRestRewriter.appendConf(sql);
        System.out.println(newSql);
        assertEquals("load Rest.`http://www.163.com` AND `config.enableRestDataSourceRequestCleaner`=\"true\" AS otu;"
                , newSql);
    }

    @Test
    public void simple2() {
        String sql = "load Rest.`http://www.163.com` AS otu  ;";
        String newSql = LoadRestRewriter.appendConf(sql);
        System.out.println(newSql);
        assertEquals("load Rest.`http://www.163.com` AND `config.enableRestDataSourceRequestCleaner`=\"true\" AS otu;"
                , newSql);
    }



    @Test
    public void multiLoadRest1() {
        String sql = "\n" +
                "LOAD Rest.`http://www.byzer.org/`\n" +
                "WHERE `config.connect-timeout`=\"10s\"\n" +
                "AND `config.page.next`= \"http://www.byzer.org/index={0}\"\n" +
                "AND `config.method`=\"get\"\n" +
                "AND `config.page.retry`=\"1\"\n" +
                "AND `config.page.values`=\"offset:0,1\"\n" +
                "AND `header.content-type`=\"application/json\"\n" +
                "ANd `config.debug`=\"true\"\n" +
                "AND `config.enableRequestCleaner`=\"true\"\n" +
                "AS dd;\n" +
                "\n" +
                "\n" +
                "select string(content) from dd as out;\n" +
                "\n" +
                "\n";
        String newSql = LoadRestRewriter.appendConf( sql ) ;
        System.out.println(newSql);
        assertTrue( newSql.startsWith("LOAD Rest.`http://www.byzer.org") );
        assertTrue( newSql.endsWith(";") );
    }

    @Test
    public void complexLoadRest() {
        String sql = "-- 刷新ZOHO token\n" +
                "SET ZOHO_URL=\"https://accounts.zoho.com/oauth/v2/token\";\n" +
                "SET REFRESH_TOKEN=\"100\";\n" +
                "SET CLIENT_ID=\"1000.42\";\n" +
                "SET CLIENT_SECRET=\"4bc\";\n" +
                "\n" +
                "LOAD Rest.`$ZOHO_URL?refresh_token=$REFRESH_TOKEN&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&grant_type=refresh_token`\n" +
                "WHERE `config.connect-timeout`=\"10s\"\n" +
                "AND `config.method`=\"POST\"\n" +
                "AND `config.retry`=\"3\"\n" +
                "AND `header.content-type`=\"application/json\"\n" +
                "AS zoho_token;\n" +
                "\n" +
                "SAVE overwrite zoho_token AS parquet.`/zoho_database/token/zoho_token`;\n" +
                "\n" +
                "LOAD parquet.`/zoho_database/token/zoho_token` as zoho_token;\n";

        final String newSql = LoadRestRewriter.appendConf(sql);
        System.out.println( newSql );
    }

    @Test
    public void splitByBlankOrLineSeparatorTest() {
        String sql = "abc\ne a";
        LoadRestRewriter.SplitResult result = splitByBlankOrLineSeparator(sql);
        assertFalse( result.splitResult );
        assertEquals( 1, result.arr.length);
    }

    @Test
    public void registerUDFTest() {
        String sql = "REGISTER ScriptUDF.`` as longToDateTime where\n" +
                "and lang=\"scala\"\n" +
                "and udfType=\"udf\"\n" +
                "and code='''\n" +
                "def apply(ts: Long ): String ={\n" +
                "val fmt  = \"yyyy-MM-dd'T'HH:mm:ss.SSSZ\"\n" +
                "val formatter = java.time.format.DateTimeFormatter.ofPattern(fmt)\n" +
                " .withZone(java.time.ZoneId.from(java.time.ZoneOffset.UTC))\n" +
                "val instant = java.time.Instant.ofEpochMilli(ts)\n" +
                "formatter.format( instant )\n" +
                "  }\n" +
                "''';\n" +
                "\n" +
                "SELECT longToDateTime(1636961897265) AS dt;";
        LoadRestRewriter.SplitResult result = splitByBlankOrLineSeparator(sql);
        assertFalse( result.splitResult );
        assertEquals( 1, result.arr.length);
    }

}
