package org.jetbrains.plugins.scala.worksheet.settings.persistent

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.FileAttribute
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetExternalRunType
import org.jetbrains.plugins.scala.worksheet.utils.FileAttributeUtilCache

private trait SerializableInFileAttribute[T] {
  def readAttribute(attr: FileAttribute, file: VirtualFile): Option[T] =
    FileAttributeUtilCache.readAttribute(attr, file).map(convertTo)

  def writeAttribute(attr: FileAttribute, file: VirtualFile, t: T): Unit =
    FileAttributeUtilCache.writeAttribute(attr, file, convertFrom(t))

  def convertFrom(t: T): String
  def convertTo(s: String): T
}

private object SerializableInFileAttribute {

  implicit val StringFileAttribute: SerializableInFileAttribute[String] = new SerializableInFileAttribute[String] {
    override def convertFrom(t: String): String = t
    override def convertTo(s: String): String = s
  }

  private val EnabledValue = "enabled"
  private val DisabledValue = "disabled"

  implicit val BooleanFileAttribute: SerializableInFileAttribute[Boolean] = new SerializableInFileAttribute[Boolean] {
    override def convertFrom(t: Boolean): String = if (t) EnabledValue else DisabledValue
    override def convertTo(s: String): Boolean = s == EnabledValue
  }

  implicit val ExternalRunTypeAttribute: SerializableInFileAttribute[WorksheetExternalRunType] = new SerializableInFileAttribute[WorksheetExternalRunType] {
    override def convertFrom(t: WorksheetExternalRunType): String = t.getName

    override def convertTo(s: String): WorksheetExternalRunType = WorksheetExternalRunType.findRunTypeByName(s).getOrElse(WorksheetExternalRunType.getDefaultRunType)
  }
}