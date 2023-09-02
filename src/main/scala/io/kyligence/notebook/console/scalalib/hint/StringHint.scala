package io.kyligence.notebook.console.scalalib.hint

import io.kyligence.notebook.console.util.SQLStringHelper
import org.apache.commons.lang3.StringEscapeUtils

import scala.collection.mutable

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class StringHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "string") {
      return query
    }

    val outputOpt = header.output
    val inputOpt = header.input

    val format = header.params.getOrElse("format", "markdown")

    val preHTML =  StringEscapeUtils.escapeJava(
      """
        |<!DOCTYPE html>
        |<html>
        |<head>
        |  <style>
        |    .custom-pre {
        |      height: 200px; /* Set the desired height */
        |      overflow: auto; /* Add scrollbars if the content exceeds the height */
        |      white-space: pre-wrap; /* Enable line wrapping */
        |    }
        |  </style>
        |</head>
        |<body>
        |  <pre class="custom-pre">
        |""".stripMargin  )

    val endPreHTML =
      StringEscapeUtils.escapeJava( """
        |</pre>
        |</body>
        |</html>
        |""".stripMargin )

    val formatSQL = format match {
      case "html" =>
        s"""select concat("${preHTML}","${SQLStringHelper.escapeSQL(header.body)}","${endPreHTML}") as content, "${format}" as mime as ${outputOpt.getOrElse("output")};"""
      case "markdown" =>
        s"""select "${SQLStringHelper.escapeSQL(header.body)}" as content, "${format}" as mime as ${outputOpt.getOrElse("output")};"""
      case _ => ""
    }

    formatSQL
  }
}
