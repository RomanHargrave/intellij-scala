package org.jetbrains.plugins.scala.compilationCharts.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.{ActionManager, ActionToolbar, AnActionEvent, DefaultActionGroup}
import com.intellij.openapi.project.{DumbAwareAction, Project}
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.{IdeBorderFactory, SideBorder}
import com.intellij.util.ui.components.BorderLayoutPanel

/**
 * Main component
 */
class CompilationProgressComponent(project: Project)
  extends BorderLayoutPanel
    with Disposable {

  private val panel = new CompilationProgressPanel(project)

  // TODO dispose?

  addToLeft(createActionToolbar().getComponent)
  addToCenter(new JBScrollPane(panel))

  override def dispose(): Unit = ()

  private def createActionToolbar(): ActionToolbar = {
    val group = new DefaultActionGroup
    val actions = Seq(
      new ZoomInAction,
      new ZoomOutAction,
      new ResetZoomAction
    )
    actions.foreach(group.add)
    val actionManager = ActionManager.getInstance
    val toolbar = actionManager.createActionToolbar("ScalaCompilationProgress", group, false)
    val border = IdeBorderFactory.createBorder(SideBorder.RIGHT)
    toolbar.getComponent.setBorder(border)
    toolbar
  }
}

class ZoomInAction
  extends DumbAwareAction("zoom in", "ZOOM IN", AllIcons.General.ZoomIn) {

  override def actionPerformed(e: AnActionEvent): Unit = ()
}

class ZoomOutAction
  extends DumbAwareAction("zoom out", "ZOOM OUT", AllIcons.General.ZoomOut) {

  override def actionPerformed(e: AnActionEvent): Unit = ()
}

class ResetZoomAction
  extends DumbAwareAction("reset zoom", "RESET ZOOM", AllIcons.General.ActualZoom) {

  override def actionPerformed(e: AnActionEvent): Unit = ()
}
