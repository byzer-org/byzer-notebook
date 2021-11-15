package io.kyligence.notebook.console.scalalib.hint

import java.util.UUID
import scala.collection.mutable

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class JDBCHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "direct-jdbc") {
      return query
    }
    val db = header.params("db")
    val tpeOpt = header.params.get("type")
    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))

    val newBody = header.body.split(";\n").filterNot(_.trim.isEmpty).zipWithIndex.map { case (sta, index) =>
      s"""
         |and `driver-statement-${index}`='''
         |${sta};
         |'''
         |""".stripMargin
    }.mkString(" ")

    tpeOpt match {
      case Some("ddl") =>
        s"""
           |run command as JDBC.`${db}._` where
           |sqlMode="ddl" ${newBody};
           |""".stripMargin
      case Some("query") | None =>

        s"""
           |load jdbc.`${db}._` where directQuery='''
           |${header.body}
           |''' as ${output};
           |""".stripMargin
    }


  }
}
