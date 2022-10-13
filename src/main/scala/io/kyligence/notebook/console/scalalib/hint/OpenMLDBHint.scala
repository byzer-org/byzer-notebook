package io.kyligence.notebook.console.scalalib.hint

import java.util.UUID
import scala.collection.mutable

/**
 * 13/10/2022 WilliamZhu(allwefantasy@gmail.com)
 */
class OpenMLDBHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t.toLowerCase != "openmldb") {
      return query
    }

    require(header.params.contains("db"), "db is required")
    require(header.params.contains("url"), "url is required")

    val db = header.params("db")
    val url = header.params("url")
    val execute_mode = header.params.getOrElse("execute_mode", "offline")
    val job_timeout = header.params.getOrElse("job_timeout", "20000000")

    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))

    s"""
       |run command as FeatureStoreExt.`` where
       |zkAddress="${url}"
       |and `sql-0`='''
       |SET @@execute_mode='${execute_mode}';
       |'''
       |and `sql-1`='''
       |SET @@job_timeout=${job_timeout};
       |'''
       |and `sql-2`='''
       |${header.body}
       |'''
       |and db="${db}"
       |and action="ddl"
       |as ${output};
       |""".stripMargin
  }
}

