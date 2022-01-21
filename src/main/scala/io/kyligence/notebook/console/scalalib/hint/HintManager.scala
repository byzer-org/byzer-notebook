package io.kyligence.notebook.console.scalalib.hint

import collection.JavaConverters._

object HintManager {

  private val noEffectHintList: List[BaseHint] =
    List(new KylinHint, new PythonHint, new JDBCHint)

  private val effectHintList: List[BaseHint] =
    List(new DeployScriptHint, new DeployModelHint,new DeployPythonModelHint)

  def applyAllHintRewrite(sql: String, options: java.util.Map[String, String]): String = {
    var tempSQL = applyNoEffectRewrite(sql, options)
    if (tempSQL == sql) {
      tempSQL = applyEffectRewrite(sql, options)
    }
    tempSQL
  }

  def applyNoEffectRewrite(sql: String, options: java.util.Map[String, String]): String = {
    val newOptions = options.asScala
    var tempSQL = sql
    noEffectHintList.foreach(hinter => {
      if (tempSQL == sql) {
        tempSQL = hinter.rewrite(tempSQL, newOptions)
      }
    })
    tempSQL
  }

  def applyEffectRewrite(sql: String, options: java.util.Map[String, String]): String = {
    val newOptions = options.asScala
    var tempSQL = sql
    effectHintList.foreach(hinter => {
      if (tempSQL == sql) {
        tempSQL = hinter.rewrite(tempSQL, newOptions)
      }
    })
    tempSQL
  }


}
