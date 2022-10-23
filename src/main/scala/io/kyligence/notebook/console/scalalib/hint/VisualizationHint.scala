package io.kyligence.notebook.console.scalalib.hint

import scala.collection.mutable

/**
 * 17/10/2022 WilliamZhu(allwefantasy@gmail.com)
 */
class VisualizationHint extends BaseHint {
  override def rewrite(query: String, options: mutable.Map[String, String]): String = {
    val header = _parse(query)
    if (header.t.toLowerCase != "visualize") {
      return query
    }

    require(header.input.isDefined, "--%input is required")

    s"""
       |!visualize ${header.input.get} '''
       |${header.body}
       |''';
       |""".stripMargin
  }
}
