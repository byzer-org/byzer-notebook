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
    val temperature = header.params.getOrElse("temperature","0.1")
    val userRole = header.params.getOrElse("user_role","")
    val assistantRole = header.params.getOrElse("assistant_role","")
    val systemMsg = header.params.getOrElse("system_msg","You are a helpful assistant. Think it over and answer the user's question correctly.")
    val output = header.output.get

    val instruction = StringEscapeUtils.escapeJava(header.body);

    s"""
       |select ${model}(llm_param(map(
       |    "system_msg",'${systemMsg}',
       |    "instruction",'${instruction}',
       |    "temperature","${temperature}",
       |    "user_role","${userRole}",
       |    "assistant_role","${assistantRole}"
       |))) as q as ${output};
       |
       |""".stripMargin
  }
}
