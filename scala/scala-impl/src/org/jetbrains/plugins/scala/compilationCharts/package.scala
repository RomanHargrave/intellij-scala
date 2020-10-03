package org.jetbrains.plugins.scala

import org.jetbrains.plugins.scala.compiler.CompilationUnitId

package object compilationCharts {

  type CompilationProgressState = Map[CompilationUnitId, CompilationProgressInfo]
  type Timestamp = Long
}
