package io.kyligence.notebook.console.scalalib.hint

import org.apache.commons.lang3.StringEscapeUtils
import scala.collection.mutable

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class ChatHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "chat") {
      return query
    }
    val model = header.params("model")
    //    val temperature = header.params.getOrElse("temperature", "0.1")
    //    val userRole = header.params.getOrElse("user_role", "")
    //    val assistantRole = header.params.getOrElse("assistant_role", "")
    //    val systemMsg = header.params.getOrElse("system_msg", "")
   val params =  header.params.map { case (k, v) =>
      s""""${k}",'${v}'"""
    }.mkString(",\n")

    val outputOpt = header.output
    val inputOpt = header.input

    val format = header.params.getOrElse("format", "html")

//    val tempTableName = UUID.randomUUID().toString .replaceAll("-", "")

    val formatSQL = if (format == "html"){
      s"""select concat("<pre>",llm_response_predict(q),"</pre>") as content, "${format}" as mime from ${outputOpt.getOrElse("output")} as byzerllm_format_table;"""
    } else ""

    val instruction = StringEscapeUtils.escapeJava(header.body.trim);

    if (inputOpt.isDefined) {
      s"""
         |select  chat(llm_stack(q,llm_param(map(
         |
         |    "instruction","${instruction}",
         |    ${params}
         |)))) as q from ${inputOpt.get} as ${outputOpt.getOrElse("output")};
         |${formatSQL}
         |""".stripMargin
    } else {
      s"""
         |select ${model}(llm_param(map(
         |    "instruction","${instruction}",
         |    ${params}
         |))) as q as ${outputOpt.getOrElse("output")};
         |${formatSQL}
         |""".stripMargin
    }

  }
}
