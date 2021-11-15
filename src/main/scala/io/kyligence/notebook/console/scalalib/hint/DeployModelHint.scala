package io.kyligence.notebook.console.scalalib.hint

import io.kyligence.notebook.console.scalalib.utils.PathFun
import org.apache.http.client.fluent.{Form, Request}

import java.nio.charset.Charset
import java.util.UUID
import scala.collection.mutable

/**
 * 8/3/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class DeployModelHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t.toLowerCase != "deployModel".toLowerCase) {
      return query
    }
    if (!header.params.contains("url")) {
      return query
    }
    val url = header.params("url")
    val owner = options("owner")
    val home = options("home")

    val formParamBuilder = Form.form()
    (header.params - "url").foreach(item => formParamBuilder.add(item._1, item._2))
    formParamBuilder.add("sql", header.body).add("owner", owner)
    formParamBuilder.add("defaultPathPrefix", PathFun(home).add(owner).toPath)
    Request.Post(url.stripSuffix("/") + "/run/script").
      bodyForm(formParamBuilder.build(), Charset.forName("utf-8")).
      execute().returnContent().
      asString(Charset.forName("utf-8"))

    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))

    s"run command as ShowFunctionsExt.`` as ${output};"
  }
}
