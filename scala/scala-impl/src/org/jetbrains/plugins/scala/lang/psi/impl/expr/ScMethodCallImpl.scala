package org.jetbrains.plugins.scala
package lang
package psi
package impl
package expr

import com.intellij.lang.ASTNode
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.ScMethodCallCfgBuildingImpl
import org.jetbrains.plugins.scala.lang.psi.impl.expr.ScMethodCallImpl._
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveResult

/**
  * @author Alexander Podkhalyuzin
  *         Date: 06.03.2008
  */
class ScMethodCallImpl(node: ASTNode)
  extends MethodInvocationImpl(node)
    with ScMethodCall with ScMethodCallCfgBuildingImpl {
  override def thisExpr: Option[ScExpression] = {
    getEffectiveInvokedExpr.asOptionOf[ScReferenceExpression].flatMap { invokedExpr =>
      val refName = invokedExpr.refName
      invokedExpr.bind() match {
        case Some(resolved) if isApplyOrUpdateOnNoFunctionObject(refName, resolved) ||
                                isApplyToFunctionObject(resolved) =>
          Some(invokedExpr)
        case _ =>
          invokedExpr.qualifier
      }
    }
  }

  override def getInvokedExpr: ScExpression = findChildByClassScala(classOf[ScExpression])

  override def argumentExpressions: Seq[ScExpression] = if (args != null) args.exprs else Nil

  override def getEffectiveInvokedExpr: ScExpression = {
    findChildByClassScala(classOf[ScExpression]) match {
      case x: ScParenthesisedExpr => x.innerElement.getOrElse(x)
      case x => x
    }
  }

  override def toString: String = "MethodCall"
}

object ScMethodCallImpl {
  private def isApplyOrUpdateOnNoFunctionObject(refName: String, resolved: ScalaResolveResult): Boolean =
    refName != resolved.name && (resolved.name == "apply" || resolved.name == "update")

  private def isApplyToFunctionObject(resolveResult: ScalaResolveResult): Boolean =
    resolveResult.innerResolveResult.exists(_.element.getName == "apply")
}
