package io.kyligence.notebook.console.scalalib.hint
import java.util.UUID

import scala.collection.mutable

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class KylinHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "kylin") {
      return query
    }
    val db = header.params("db")
    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))

    s"""
       |load jdbc.`${db}._` where directQuery='''
       |${header.body}
       |''' as ${output};
       |""".stripMargin

  }
}
