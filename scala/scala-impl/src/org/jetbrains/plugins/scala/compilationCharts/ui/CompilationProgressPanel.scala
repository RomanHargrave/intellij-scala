package org.jetbrains.plugins.scala.compilationCharts.ui

import java.awt.geom.Rectangle2D
import java.awt.{Color, Graphics, Graphics2D}

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import org.jetbrains.plugins.scala.compilationCharts.ui.CompilationProgressPanel.CompilationInterval
import org.jetbrains.plugins.scala.compilationCharts.{CompilationProgressInfo, CompilationProgressState, CompilationProgressStateManager, Timestamp}
import org.jetbrains.plugins.scala.compiler.CompilationUnitId

class CompilationProgressPanel(project: Project)
  extends JBPanelWithEmptyText
    with Disposable {

  // TODO dispose?

  private val rectHeight = new JBTable().getRowHeight

  setBackground(UIUtil.getTreeBackground) // TODO more common

  override def dispose(): Unit = ()

  override def paintComponent(graphics: Graphics): Unit = {
    val state = CompilationProgressStateManager.get(project)
    val intervals = getCompilationIntervals(state)
    if (intervals.isEmpty) {
      super.paintComponents(graphics)
    } else {
      val componentWidth = getWidth
      intervals.zipWithIndex.foreach { case (CompilationInterval(label, from, to, isTest), i) =>
        val x = math.round(from * componentWidth).toInt
        val y = rectHeight * i
        val width = math.round((to - from) * componentWidth).toInt
        val height = rectHeight
        val rectGraphics = graphics.create(x, y, width, height).asInstanceOf[Graphics2D]
        paintRect(label, isTest, rectGraphics)
      }
    }
  }

  private def paintRect(label: String, isTest: Boolean, graphics: Graphics2D): Unit = {
    val clipBounds = graphics.getClipBounds
    val transform = graphics.getTransform
    val scaleX = 1 / transform.getScaleX
    val scaleY = 1 / transform.getScaleY

    val borderRect = new Rectangle2D.Double(
      clipBounds.x,
      clipBounds.y,
      clipBounds.width,
      clipBounds.height
    ) // TODO better borders?
    val borderColor = ColorUtil.mix(getBackground, Color.BLACK, 0.6)
    graphics.setColor(borderColor)
    graphics.fill(borderRect)

    val filledRect = new Rectangle2D.Double(
      borderRect.x + scaleX,
      borderRect.y + scaleY,
      borderRect.getWidth - scaleX * 2,
      borderRect.getHeight - scaleY * 2
    )
    val color = if (isTest)
      new Color(98, 181, 67)
    else
      new Color(64, 182, 224)
    val fillColor = ColorUtil.mix(getBackground, color, 0.6)
    graphics.setColor(fillColor)
    graphics.fill(filledRect)

    graphics.setColor(Color.BLACK/*UIUtil.getActiveTextColor*/) // TODO better color? TODO
    val fontMetrics = graphics.getFontMetrics(graphics.getFont)
    val x = clipBounds.width / 2 - fontMetrics.getStringBounds(label, graphics).getWidth.toFloat / 2
    val y = clipBounds.height.toFloat / 2 + fontMetrics.getAscent * 2 / 5
    graphics.drawString(label, x, y) // TODO what to do, if the label doesn't fit to the borders?
  }

  private def getCompilationIntervals(state: CompilationProgressState): Seq[CompilationInterval] = {
    val sortedTimes = state.values.toSet.flatMap { progressInfo: CompilationProgressInfo =>
      Set(progressInfo.startTime, progressInfo.updateTime) ++ progressInfo.finishTime.toSet
    }.toList.sorted
    sortedTimes match {
      case minTime :: tail =>
        val maxTime = tail.lastOption.getOrElse(minTime)

        def position(time: Timestamp): Double =
          (time - minTime).toDouble / (maxTime - minTime)

        val sortedState = state.toSeq.sortBy(_._2.startTime)
        val intervals = sortedState.map { case (compilationUnitId, progressInfo) =>
          val CompilationProgressInfo(startTime, finishTime, updateTime, progress) = progressInfo
          val CompilationUnitId(moduleName, testScope) = compilationUnitId

          val percent = math.min(100, math.round(progress * 100))
          val label = s"$moduleName"
          val (from, to) = if (minTime == maxTime)
            (0.0, 1.0)
          else
            (position(startTime), position(finishTime.getOrElse(updateTime)))
          CompilationInterval(label, from, to, testScope)
        }
        intervals
      case Nil =>
        Seq.empty
    }
  }
}

object CompilationProgressPanel {

  private final case class CompilationInterval(label: String,
                                               from: Double,
                                               to: Double,
                                               isTest: Boolean)
}
