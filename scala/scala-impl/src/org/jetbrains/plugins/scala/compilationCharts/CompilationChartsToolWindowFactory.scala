package org.jetbrains.plugins.scala.compilationCharts

import java.awt.{Color, Dimension, Graphics}
import java.util.concurrent.CompletableFuture

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.{Service, ServiceManager}
import com.intellij.openapi.project.{DumbAware, Project}
import com.intellij.openapi.wm.{ToolWindow, ToolWindowFactory}
import com.intellij.ui.content.ContentFactory
import javax.swing.{JComponent, JLabel, JPanel}
import org.jetbrains.plugins.scala.compilationCharts.CompilationChartsToolWindowFactory._
import org.jetbrains.plugins.scala.compilationCharts.ui.CompilationProgressComponent
import org.jetbrains.plugins.scala.compiler.CompilationUnitId
import org.jetbrains.plugins.scala.extensions.invokeLater
import org.jetbrains.plugins.scala.project.ProjectExt
import org.jetbrains.plugins.scala.util.ui.extensions.ComponentExt

final class CompilationChartsToolWindowFactory
  extends ToolWindowFactory
    with DumbAware {

  override def init(toolWindow: ToolWindow): Unit = {
    toolWindow.setStripeTitle("Compilation charts")
  }

  override def isApplicable(project: Project): Boolean =
    isVisibleFor(project)

  override def shouldBeAvailable(project: Project): Boolean =
    isVisibleFor(project)

  override def createToolWindowContent(project: Project, toolWindow: ToolWindow): Unit = {
    val factory = ContentFactory.SERVICE.getInstance
    val content = factory.createContent(initMainComponent(project), "Compilation", true)
    toolWindow.getContentManager.addContent(content)
  }
}

object CompilationChartsToolWindowFactory {

  def refresh(project: Project): Unit =
    MainComponentHolder.get(project).mainComponent.foreach { mainPanel =>
      refresh(project, mainPanel)
    }

  private def initMainComponent(project: Project): JComponent = {
    val mainComponent = new CompilationProgressComponent(project)
    MainComponentHolder.get(project).mainComponent = Some(mainComponent)
    mainComponent.bindExecutionToVisibility { () =>
      CompletableFuture.completedFuture(CompilationChartsToolWindowFactory.refresh(project, mainComponent))
    }
    mainComponent
  }

  private def isVisibleFor(project: Project): Boolean =
    ApplicationManager.getApplication.isInternal && project.hasScala

  @Service
  private final class MainComponentHolder {
    var mainComponent: Option[JComponent] = None
  }

  private object MainComponentHolder {

    def get(project: Project): MainComponentHolder =
      ServiceManager.getService(project, classOf[MainComponentHolder])
  }

  private def refresh(project: Project, mainComponent: JComponent): Unit = ()
//    if (mainComponent.isVisible) {
//      val state = CompilationProgressStateManager.get(project)
//      val text = render(state)
//      val compilationIntervals = getCompilationIntervals(state)
//
//      invokeLater {
//        mainComponent.removeAll()
//        val panel = new JPanel
//        panel.setLayout(new GridLayout(0, 1, 0, 0))
//
//        compilationIntervals.foreach { case CompilationInterval(label, from, to) =>
//          panel.add(new MyPanel(label, from, to))
//        }
//        val scrollPane = ScrollPaneFactory.createScrollPane(panel)
//        scrollPane.setPreferredSize(new Dimension(1000, 500))
//        mainComponent.add(scrollPane)
//      }
//    }
}
