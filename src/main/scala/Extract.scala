package Chisel
import Node._
import Component._
import Lit._

object NodeExtract {
  // extract one bit
  def apply(mod: Node, bit: Int): Node = apply(mod, bit, bit)

  // extract one bit
  def apply(mod: Node, bit: Node): Node = {
    val bitLit = bit.litOf
    if (bitLit != null)
      apply(mod, bitLit.value.toInt)
    else if (mod.litOf == null)
      makeExtract(mod, bit)
    else // don't use Extract on literals
      Op(">>", 0, fixWidth(1), mod, bit)
  }

  // extract bit range
  def apply(mod: Node, hi: Int, lo: Int): Node = apply(mod, hi, lo, -1)
  def apply(mod: Node, hi: Int, lo: Int, width: Int): Node = {
    val w = if (width == -1) hi - lo + 1 else width
    val bits_lit = mod.litOf
    if (bits_lit != null)
      Literal((bits_lit.value >> lo) & ((BigInt(1) << w) - BigInt(1)), w)
    else
      makeExtract(mod, Literal(hi), Literal(lo), fixWidth(w))
  }

  // extract bit range
  def apply(mod: Node, hi: Node, lo: Node, width: Int = -1): Node = {
    val hiLit = hi.litOf
    val loLit = lo.litOf
    val widthInfer = if (width == -1) widthOf(0) else fixWidth(width)
    if (hiLit != null && loLit != null)
      apply(mod, hiLit.value.toInt, loLit.value.toInt, width)
    else if (mod.litOf == null)
      makeExtract(mod, hi, lo, widthInfer)
    else { // don't use Extract on literals
      val rsh = Op(">>", 0, widthInfer, mod, lo)
      val hiMinusLoPlus1 = Op("+", 2, maxWidth _, Op("-", 2, maxWidth _, hi, lo), UFix(1))
      val mask = Op("-", 2, widthInfer, Op("<<", 0, widthInfer, UFix(1), hiMinusLoPlus1), UFix(1))
      Op("&", 2, widthInfer, rsh, mask)
    }
  }

  private def makeExtract(mod: Node, bit: Node) = {
    val res = new Extract
    res.init("", fixWidth(1), mod, bit)
    res.hi = bit
    res.lo = bit
    res
  }

  private def makeExtract(mod: Node, hi: Node, lo: Node, width: (Node) => Int) = {
    val res = new Extract
    res.init("", width, mod, hi, lo)
    res.hi = hi
    res.lo = lo
    res
  }
}

object Extract {
  //extract 1 bit
  def apply[T <: Bits](mod: T, bit: UFix)(gen: => T): T = {
    val x = NodeExtract(mod, bit)
    x.setTypeNodeNoAssign(gen.fromNode(x))
  }

  def apply[T <: Bits](mod: T, bit: Int)(gen: => T): T = {
    val x = NodeExtract(mod, bit)
    x.setTypeNodeNoAssign(gen.fromNode(x))
  }

  // extract bit range
  def apply[T <: Bits](mod: T, hi: UFix, lo: UFix, w: Int = -1)(gen: => T): T = {
    val x = NodeExtract(mod, hi, lo, w)
    x.setTypeNodeNoAssign(gen.fromNode(x))
  }

  def apply[T <: Bits](mod: T, hi: Int, lo: Int)(gen: => T): T ={
    val x = NodeExtract(mod, hi, lo)
    x.setTypeNodeNoAssign(gen.fromNode(x))
  }
}

class Extract extends Node {
  var lo: Node = null;
  var hi: Node = null;

  override def toString: String =
    if (hi == lo)
      "BITS(" + inputs(0) + ", " + lo + ")";
    else
      "BITS(" + inputs(0) + ", " + hi + ", " + lo + ")";
  def validateIndex(x: Node) = {
    val lit = x.litOf
    assert(lit == null || lit.value >= 0 && lit.value < inputs(0).width, 
           {println("Extract(" + lit.value + ")" +
                    " out of range [0," + (inputs(0).width-1) + "]" +
                    " of " + inputs(0) +
                    " on line " + line.getLineNumber +
                    " in class " + line.getClassName + 
                    " in file " + line.getFileName)
          }
         )
  }
}
