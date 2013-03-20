package Chisel
import Component._
import Node._

object Cat {
  private def doit[T <: Data](mods: List[T]): Bits = {
    val modsList = mods.filter(_ != null).map(_.toNode)
    val isLit = isFolding && modsList.forall(_.litOf != null)
    val res = if (!isLit && backend.isInstanceOf[VerilogBackend]) {
      val res = new Cat();
      res.initOf("", sumWidth _, modsList)
    } else
      modsList.reduceLeft((a, b) => a ## b)
    res.setTypeNode(Bits(OUTPUT))
  }

  def apply[T <: Data](mod: T, mods: T*): Bits = doit(mod :: mods.toList)
  def apply(mod: UFix, mods: UFix*): UFix = doit(mod :: mods.toList).toUFix
}

class Cat extends Node {
}

object Concatenate {
  def apply (mod: Node, mods: Node*): Node = {
    val isLit = isFolding && mods.toList.forall(_.litOf != null)
    if(!isLit && backend.isInstanceOf[VerilogBackend]) {
      val res = new Cat();
      res.initOf("", sumWidth _, mod :: mods.toList);
      res
    } else
      mods.foldLeft(mod){(a, b) => a ## b};
  }
}

