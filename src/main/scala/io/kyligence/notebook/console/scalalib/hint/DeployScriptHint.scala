package io.kyligence.notebook.console.scalalib.hint

import org.apache.http.client.fluent.{Form, Request}

import java.nio.charset.Charset
import java.util.UUID
import scala.collection.mutable

/**
 * 31/5/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class DeployScriptHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t.toLowerCase != "deployScript".toLowerCase) {
      return query
    }

    require(header.params.contains("url"), "--%url is required")
    require(header.params.contains("codeName"), "--%codeName is required")

    val url = header.params("url")
    //    val codeName = header.params("codeName")

    val owner = options("owner")
    val home = options("home")

    val formParamBuilder = Form.form()
    (header.params - "url").foreach(item => formParamBuilder.add(item._1, item._2))
    formParamBuilder.add("code", header.body).add("owner", owner)
    formParamBuilder.add("executeMode", "pyRegister")
    Request.Post(url.stripSuffix("/") + "/run/script").
      bodyForm(formParamBuilder.build(), Charset.forName("utf-8")).
      execute().returnContent().
      asString(Charset.forName("utf-8"))

    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))

    s"""
       |select '${url.stripSuffix("/")}/run/script?executeMode=pyPredict&codeName=&env=' as predictUrl as $output;
       |""".stripMargin
  }
}