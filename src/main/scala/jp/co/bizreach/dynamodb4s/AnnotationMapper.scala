package jp.co.bizreach.dynamodb4s

import reflect.ClassTag
import reflect.runtime._
import universe._

class AnnotationMapper[T: TypeTag: ClassTag] {
  /** Annotated ClassSymbol */
  lazy val classSymbol: Symbol = typeOf[T].typeSymbol.asClass

  /** Annotated class member Symbol */
  lazy val memberSymbols: Seq[Symbol] = typeOf[T].member(nme.CONSTRUCTOR).typeSignature match {
    case MethodType(params, _) => params
  }

  /** Annotated class member Symbol */
  lazy val annotatedMemberSymbols: Seq[Symbol] = memberSymbols.filter(_.annotations.size > 0)

  /** Not annotated class member Symbol */
  lazy val notAnnotatedMemberSymbols: Seq[Symbol] = memberSymbols.filter(_.annotations.size == 0)

  /**
   * Returns the Symbol specified the annotation type.
   * @tparam A annotation class
   * @return the Symbol representing the member
   */
  def getAs[A: TypeTag]: Option[Symbol] = annotatedMemberSymbols.find(annotation[A](_).isDefined)

  /**
   * Returns the Annotation specified the Symbol of the annotation type.
   * @param m the member
   * @tparam A annotation class
   * @return the Annotation granted to member
   */
  def annotation[A: TypeTag](m: Symbol): Option[Annotation] = m.annotations.find(_.tpe <:< typeOf[A])

  /**
   * Returns the Annotation attribute.
   * Note that the attribute type allows only string, the other throws Exception.
   * @param a the annotation
   * @return the annotation attribute list
   */
  def annotationAttribute(a: Annotation): Seq[String] = a.scalaArgs.map {
    case Literal(Constant(name: String)) => name
    case _ => throw new Exception("invalid argument to static annotation: allow only string")
  }

  /**
   * Returns the value stored in the field.
   * @param instance
   * @param m
   * @return
   */
  def getValue(instance: T, m: Symbol): Any = {
    currentMirror
      .reflect(instance)
      .reflectField(typeOf[T].member(m.name).asTerm)
      .get
  }



}
