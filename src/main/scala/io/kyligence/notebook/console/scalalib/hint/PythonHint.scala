package io.kyligence.notebook.console.scalalib.hint

import java.util.UUID

import scala.collection.mutable

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class PythonHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t != "python") {
      return query
    }
    val input = header.input.getOrElse("command")
    val output = header.output.getOrElse(UUID.randomUUID().toString.replaceAll("-", ""))
    val cache = header.params.getOrElse("cache", "false").toBoolean
    var cacheStr =
      s"""
         |save overwrite ${output}_0 as parquet.`/tmp/__python__/${output}`;
         |load parquet.`/tmp/__python__/${output}` as ${output};
         |""".stripMargin

    if (!cache) {
      cacheStr = s"select * from ${output}_0 as ${output};"
    }

    val confTableOpt = header.params.get("confTable").map(item => s""" confTable="${item}" and """).getOrElse("")
    val model = header.params.get("model").map(item => s""" model="${item}" and """).getOrElse("")
    val schema = header.params.get("schema").map(item => s""" !python conf '''schema=${item}'''; """).getOrElse("")
    val env = header.params.get("env").map(item => s""" !python env '''PYTHON_ENV=${item}'''; """).getOrElse("")
    val dataMode = header.params.get("dataMode").map(item => s""" !python conf '''dataMode=${item}'''; """).getOrElse("")
    val runIn = header.params.get("runIn").map(item => s""" !python conf '''runIn=${item}'''; """).getOrElse("")

    s"""
       |${schema}
       |${env}
       |${dataMode}
       |${runIn}
       |run command as Ray.`` where
       |inputTable="${input}" and
       |outputTable="${output}_0" and
       |${confTableOpt}
       |${model}
       |code='''${header.body}''';
       |${cacheStr}
       |""".stripMargin

  }
}
