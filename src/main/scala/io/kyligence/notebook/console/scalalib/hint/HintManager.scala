package io.kyligence.notebook.console.scalalib.hint

import collection.JavaConverters._

object HintManager {

  private val hintList: List[BaseHint] = initHinters

  private def initHinters(): List[BaseHint] = {
    List(new KylinHint, new PythonHint, new DeployScriptHint,
      new DeployModelHint,new DeployPythonModelHint, new JDBCHint)
  }

  def applyHintRewrite(sql: String, options: java.util.Map[String, String]): String = {
    val newOptions = options.asScala
    var tempSQL = sql
    hintList.foreach(hinter => {
      if (tempSQL == sql) {
        tempSQL = hinter.rewrite(tempSQL, newOptions)
      }
    })
    tempSQL
  }


}
