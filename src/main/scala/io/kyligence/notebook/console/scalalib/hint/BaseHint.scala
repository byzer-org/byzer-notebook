package io.kyligence.notebook.console.scalalib.hint

import scala.collection.mutable

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
trait BaseHint {
  def rewrite(query: String, options: mutable.Map[String, String]): String

  protected def _parse(query: String): SQLHeadHint = {
    val headers = query.split("\n").filter(item =>
      item.stripMargin.startsWith("--%") || item.stripMargin.startsWith("#%")
    ).map { item =>
      item.stripMargin.stripPrefix("--%").stripPrefix("#%")
    }

    val body = query.split("\n").map { item =>
      if (item.stripMargin.startsWith("--%") || item.stripMargin.startsWith("#%")) {
        ""
      } else item
    }.mkString("\n")


    var t: String = "mlsql"
    var input: Option[String] = None
    var output: Option[String] = None
    val headerParams = scala.collection.mutable.HashMap[String, String]()
    headers.foreach { header =>
      if (!header.contains("=")) {
        t = header
      } else {
        val Array(k, _v) = header.split("=", 2)
        val v = _v
        k match {
          case "input" =>
            input = Some(v)
          case "output" =>
            output = Some(v)
          case _ =>
            headerParams += (k -> v)
        }
      }
    }
    SQLHeadHint(t, body, input, output, headerParams.toMap)
  }
}

case class SQLHeadHint(t: String, body: String, input: Option[String], output: Option[String], params: Map[String, String])
