package class_instance_refinement_tool

import scala.io.Source
import scala.collection.mutable.ListBuffer
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream

/**
 * 2. Refining class-instance relationships and identifying alignment target classes
 * 2-6. class_instance_refinement_tool.AnalyzeClassInstanceExperiments.scala
 * - Inputs
 * -- inputs_and_outputs/class-list_from_role.txt
 * -- inputs_and_outputs/class-list_from_type.txt
 * -- inputs_and_outputs/class-instance-refinement-results-20120302.txt
 * - Output
 * -- inputs_and_outputs/alignment-target-class-list.txt
 */
object AnalyzeClassInstanceExperiments {
  def main(args: Array[String]) {
    val classListFromRole = "inputs_and_outputs/class-list_from_role.txt"
    val classListFromType = "inputs_and_outputs/class-list_from_type.txt"
    val classInstanceRefinementResults = "inputs_and_outputs/class-instance-refinement-results-20120302.txt"
    val alignmentTargetClassListFile = "inputs_and_outputs/alignment-target-class-list.txt"

    val clsListFromRole = ListBuffer[String]()
    for (c <- Source.fromFile(classListFromRole).getLines) {
      clsListFromRole += c
    }
    val clsList = ListBuffer[String]()
    for (c <- Source.fromFile(classListFromType).getLines) {
      clsList += c
    }
    println("クラス数(Role): " + clsListFromRole.toSet.size)
    println("クラス数(type): " + clsList.toSet.size)
    println("クラス数(all): " + (clsListFromRole.toSet ++ clsList.toSet).size)

    val alignmentTargetClsList = ListBuffer[String]()
    val wrongClsList = ListBuffer[String]()
    var refinedClassNum = 0
    var supClassAddedNum = 0
    for (line <- Source.fromFile(classInstanceRefinementResults).getLines) {
      val Array(isCorrect, orgCls, refinedCls, supCls) = line.split("\t")
      if (isCorrect == "true") {
        if (orgCls != refinedCls) {
          refinedClassNum += 1
        }
        if (supCls == "-") {
          alignmentTargetClsList += refinedCls
        } else {
          alignmentTargetClsList += supCls
          supClassAddedNum += 1
        }
      } else if (isCorrect == "false") {
        wrongClsList += orgCls
      }
    }

    println("修正されたクラス数: " + refinedClassNum)
    println("上位クラスを設定したクラス数: " + supClassAddedNum)
    println("誤ったクラス数(Role): " + (clsListFromRole.toSet & wrongClsList.toSet).size)
    println("誤ったクラス数(type): " + (clsList.toSet & wrongClsList.toSet).size)
    println("誤ったクラス数(all): " + wrongClsList.toSet.size)
    println("アライメント対象クラス数: " + alignmentTargetClsList.toSet.size)

    val correctClsSetFromRole = clsListFromRole.toSet -- wrongClsList.toSet
    val correctClsSet = clsList.toSet -- wrongClsList.toSet
    val correctClsSetAll = correctClsSetFromRole ++ correctClsSet
    println("洗練後のクラス数(Role): " + correctClsSetFromRole.size);
    println("洗練後のクラス数(Type): " + correctClsSet.size);
    println("洗練後のクラス数(All): " + correctClsSetAll.size);

    val correctAddedClsSet = correctClsSetFromRole -- correctClsSet
    println("新規にRoleより追加されたクラス数: " + correctAddedClsSet.size)
    println(correctAddedClsSet)

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(alignmentTargetClassListFile), "UTF-8"))
    val clsSet = alignmentTargetClsList.toSet
    for (cls <- clsSet) {
      writer.write(cls)
      writer.newLine()
    }
    writer.close
  }
}