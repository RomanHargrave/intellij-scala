package org.jetbrains.plugins.scala.lang.psi.types.api

import org.jetbrains.plugins.scala.lang.psi.ElementScope
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScTypeAliasDefinition
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScTrait, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.types.{AliasType, ScParameterizedType, ScType, ScalaType, api}

import scala.annotation.tailrec
import scala.reflect.ClassTag

/**
  * @author adkozlov
  */
sealed trait FunctionTypeFactory[D <: ScTypeDefinition, T] {

  import FunctionTypeFactory._

  val TypeName: String

  def apply(t: T)(implicit scope: ElementScope): ValueType

  def unapply(`type`: ScType): Option[T] =
    extractForPrefix(`type`, TypeName) match {
      case seq if seq.nonEmpty && unapplyCollector.isDefinedAt(seq) => Some(unapplyCollector(seq))
      case _ => None
    }

  protected final def apply(parameters: collection.Seq[ScType], suffix: String)
                           (implicit scope: ElementScope, tag: ClassTag[D]): ValueType =
    scope.getCachedClass(TypeName + suffix).collect {
      case definition: D => ScParameterizedType(ScalaType.designator(definition), parameters.toSeq)
    }.getOrElse(api.Nothing)

  protected def unapplyCollector: PartialFunction[collection.Seq[ScType], T]
}

object FunctionTypeFactory {

  @tailrec
  private def extractForPrefix(`type`: ScType, prefix: String, depth: Int = 100): collection.Seq[ScType] = `type` match {
    case _ if depth == 0 => Seq.empty //hack for https://youtrack.jetbrains.com/issue/SCL-6880 to avoid infinite loop.
    case AliasLowerBound(lower) => extractForPrefix(lower, prefix, depth - 1)
    case ParameterizedType(designator, arguments) if extractQualifiedName(designator).exists(_.startsWith(prefix)) => arguments
    case _ => Seq.empty
  }

  private[this] def extractQualifiedName(`type`: ScType) =
    `type`.extractClass.collect {
      case definition: ScTypeDefinition => definition
    }.flatMap(definition => Option(definition.qualifiedName))

  private[this] object AliasLowerBound {

    def unapply(`type`: ScType): Option[ScType] = `type` match {
      case AliasType(_: ScTypeAliasDefinition, Right(lower), _) => Option(lower)
      case _                                                    => None
    }
  }

}

object FunctionType extends FunctionTypeFactory[ScTrait, (ScType, collection.Seq[ScType])] {

  override val TypeName = "scala.Function"

  def traitsNames: Seq[String] = (0 to 22).map(TypeName + _)

  override def apply(pair: (ScType, collection.Seq[ScType]))
                    (implicit scope: ElementScope): ValueType = {
    val (returnType, parameters) = pair
    apply(parameters :+ returnType, parameters.length.toString)
  }

  def isFunctionType(`type`: ScType): Boolean = unapply(`type`).isDefined

  override protected def unapplyCollector: PartialFunction[collection.Seq[ScType], (ScType, collection.Seq[ScType])] = {
    case types => (types.last, types.dropRight(1))
  }
}

object PartialFunctionType extends FunctionTypeFactory[ScTrait, (ScType, ScType)] {

  override val TypeName = "scala.PartialFunction"

  override def apply(pair: (ScType, ScType))
                    (implicit scope: ElementScope): ValueType = {
    val (returnType, parameter) = pair
    apply(Seq(parameter, returnType), "")
  }

  override protected def unapplyCollector: PartialFunction[collection.Seq[ScType], (ScType, ScType)] = {
    case collection.Seq(returnType, parameter) => (parameter, returnType)
  }
}

object TupleType extends FunctionTypeFactory[ScClass, collection.Seq[ScType]] {

  override val TypeName = "scala.Tuple"

  override def apply(types: collection.Seq[ScType])
                    (implicit scope: ElementScope): ValueType =
    apply(types, types.length.toString)

  def isTupleType(`type`: ScType): Boolean = unapply(`type`).isDefined

  override protected def unapplyCollector: PartialFunction[collection.Seq[ScType], collection.Seq[ScType]] = {
    case types => types
  }
}
