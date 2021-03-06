package org.jetbrains.jps.incremental.scala.local.zinc

import java.util.Optional

import org.jetbrains.jps.incremental.scala.Client
import org.jetbrains.plugins.scala.compiler.data.CompilationData
import sbt.internal.inc._
import xsbti.api.AnalyzedClass
import xsbti.{VirtualFile, VirtualFileRef}
import xsbti.compile._

import scala.jdk.CollectionConverters._

case class IntellijExternalLookup(compilationData: CompilationData, client: Client, isCached: Boolean)
  extends ExternalLookup {

  private val all: Set[VirtualFileRef] = compilationData.zincData.allSources
    .map(file => Utils.virtualFileConverter.toVirtualFile(file.toPath))
    .toSet
  private val changedSources: Set[VirtualFileRef] = compilationData.sources
    .map(file => Utils.virtualFileConverter.toVirtualFile(file.toPath))
    .toSet

  override def lookupAnalyzedClass(binaryClassName: String, file: Option[VirtualFileRef]): Option[AnalyzedClass] =
    None

  override def changedSources(previousAnalysis: CompileAnalysis): Option[Changes[VirtualFileRef]] =
    if (isCached) None else {
    val previousSources = previousAnalysis.readStamps().getAllSourceStamps
      .keySet().asScala.toSet

    Some(new UnderlyingChanges[VirtualFileRef] {
      override def added: Set[VirtualFileRef] = all -- previousSources

      override def removed: Set[VirtualFileRef] = previousSources -- all

      override def changed: Set[VirtualFileRef] = changedSources & previousSources

      override def unmodified: Set[VirtualFileRef] = previousSources -- changedSources
    })
  }

  override def changedBinaries(previousAnalysis: CompileAnalysis): Option[Set[VirtualFileRef]] = Some(Set.empty)

  override def removedProducts(previousAnalysis: CompileAnalysis): Option[Set[VirtualFileRef]] = Some(Set.empty)

  override def shouldDoIncrementalCompilation(changedClasses: Set[String], analysis: CompileAnalysis): Boolean = {
    if (compilationData.zincData.isCompile){
      def invalidateClass(source: VirtualFileRef): Unit =
        client.sourceStarted(Utils.virtualFileConverter.toPath(source).toFile.getAbsolutePath)

      changedClasses.flatMap(analysis.asInstanceOf[Analysis].relations.definesClass).foreach(invalidateClass)
    }

    !compilationData.zincData.isCompile
  }

  override def hashClasspath(classpath: Array[VirtualFile]): Optional[Array[FileHash]] = Optional.empty()
}