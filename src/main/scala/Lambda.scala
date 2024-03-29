/**
 * Scala project to learn the language.  I'm proud of this, so don't take it
 * from me.
 *
 * wesley.bitomski@gmail.com
 * Bits: 4.6.2012
 */
package com.github.wesleybits

abstract class Expr 
case class Fun(arg:Sym, body:Expr) extends Expr {
  override def toString = "^" + arg + " " + body
}
case class Sym(name:String) extends Expr {
  override def toString = name
}
case class App(fn:Expr, args:List[Expr]) extends Expr {
  override def toString = "(" + fn + " " + args.mkString(" ") + ")"
}
case class Let(arg:Sym, vlu:Expr) extends Expr {
  override def toString = "> " + arg + " := " + vlu
}

/**
 * Simple character stream that turns a string into an iterator over that
 * string, replacing new lines with spaces.  It terminates with a null
 * character ('\0')
 */
class StringStream(s:String) { 
  private val stream = s.toList
			.map(ch => if (ch == '\n') ' ' else ch)
			.iterator
  var curr = stream.next
  def has(ch:Char):Boolean = { 
    if (ch == curr) { next; true}
    else false
  }
  def mustHave(ch:Char):Boolean = { 
    if (has(ch)) true
    else sys error "I wanted " + ch + " but you gave me " + curr
  }
  def next():Char = { 
    if (! stream.isEmpty) { curr = stream.next; curr }
    else '\0'
  }
}

/**
 * Expression Parser
 * This takes a lambda expression and creates an expression structure that can
 *  then be evaluated.  Care must be taken as to how the expression is
 *  evaluated.
 * Lets are written like:
 *  > [symbol] := [expression]
 * And expressions are::
 *  Symbol   =:: [a-z]+
 *  Function =:: ^[Symbol] [Expression]
 *  Apply    =:: ([Expression] [Expression]*)
 *  Number   =:: [0-9]+
 * Spaces and new lines are more-or-less ignored, but are required to
 *  delimit symbols. Numbers are implicitly converted to church numerals.  For style hints
 *  check the Lambda Evaluator documentation for what is essentially the kiss of death
 *  for this program.
 */
object parse { 
  def parseFun(stream:StringStream):Fun = { 
    stream has '^'
    Fun(parseSymbol(stream), parseExpr(stream))
  }
  def parseApp(stream:StringStream):App = { 
    def parseArgs():List[Expr] = { 
      if (stream has ')') Nil
      else parseExpr(stream) :: parseArgs
    }
    stream has '('
    App(parseExpr(stream), parseArgs)
  }
  def parseNumber(stream:StringStream):Expr = { 
    def num2church(num:Int):Expr = { 
      def appIter(i:Int):Expr = { 
	if (i == 0) Sym("x")
	else App(Sym("f"), List(appIter(i - 1)))
      }
      Fun(Sym("f"), Fun(Sym("x"), appIter(num)))
    }
    def collectNum(ch:Char):List[Char] = { 
      if ((ch >= '0') && (ch <= '9')) 
	ch :: collectNum(stream.next)
      else Nil
    }
    num2church((collectNum(stream.curr)).mkString.toInt)
  }
  def parseSymbol(stream:StringStream):Sym = { 
    def collectName(ch:Char):List[Char] = { 
      if ((ch >= 'a') && (ch <= 'z')) { 
	ch :: collectName(stream.next)
      } else Nil
    }
    Sym(collectName(stream.curr).mkString)
  }
  def parseLet(stream:StringStream):Let = { 
    stream has '>'; stream has ' '
    val sym = parseSymbol(stream)
    stream has ' '
    stream mustHave ':'; stream mustHave '='; stream has ' '
    val defn = parseExpr(stream)
    Let(sym,defn)
  }
  def parseExpr(stream:StringStream):Expr = {
    while(stream.curr == ' ') { stream.next }
    stream.curr match { 
      case '^' => parseFun(stream)
      case '(' => parseApp(stream)
      case '>' => parseLet(stream)
      case _ => { 
	val ch = stream.curr
	if ((ch >= '0') && (ch <= '9')) parseNumber(stream)
	else parseSymbol(stream)
      }
    }
  }
  def apply(source:String):Expr = { 
    parseExpr(new StringStream(source))
  }
}

/**
 * Lambda Evaluator: It performs lambda calculus using the replace and eval
 * technique, and the only environment remembered is the global one.  It has
 * implicit currying with implicit lexical scope.  Be certain, though, to
 * avoid the Y-combinator, it will cause the interpreter to loop endlessly.
 */

object eval { 
  type Frame = List[(String,Expr)]
  private var globalEnv:Frame = Nil

  def bind(name:String, defn:Expr):Unit = { 
    globalEnv = (name, defn) :: globalEnv
  }
  def lookup(itm:String):Expr = { 
    val trye = globalEnv.find(_._1 == itm).orNull
    if (trye == null) Sym(itm)
    else trye._2
  }
  /**
   * Replace occurances of <sym>bol with <defn>ition <in> the expression.
   * If a Symbol is found the same name as our <sym>bol, replace it.
   * If a function is found, run replace into it's body only if it's argument
   *   doesn't have the same name as our <sym>bol
   * If an Apply is found, run replace on it's fn, and then on it's args
   */
  def replace(sym:String, defn:Expr, in:Expr):Expr = in match { 
    case Sym(name)      => if (name == sym) defn
			   else in
    case App(fn, args)  => App(replace(sym,defn,fn), args.map(replace(sym,defn,_)))
    case Fun(param, body) => if (sym == param.name) in
			   else Fun(param, replace(sym,defn,body))
  }


  /**
   * Evaluate An Application of <fn> on <args>
   * If <fn> is a Function, eat one of it's parameters with an arg, and
   *  replace it's occurances in its body
   * If <fn> is a Symbol, replace it with it's meaning, and either return an
   *  Application with <fn>'s replacement and evaluated <args>, replace that
   *  Symbol with it's meaning and Reapply
   * Otherwise, evaluate <fn> and repapply with <args>
   */
  def evalApp(fn:Expr, args:List[Expr]):Expr = 
    if (args.isEmpty) evalExpr(fn)
    else fn match { 
      case Fun(param, body) => evalApp(replace(param.name, args.head, body),
				     args.tail)
      case Sym(name) => { 
	val trye = lookup(name); trye match { 
	  case Sym(name2) => App(trye, args.map(evalExpr(_)))
	  case _ => evalApp(trye, args)
	}
      }
      case App(fn2, args2) => { 
	val trye = evalApp(fn2, args2); trye match { 
	  case App(fn3, args3) => App(trye, args.map(evalExpr(_)))
	  case _ => evalApp(trye, args)
	}
      }
      case _ => evalApp(evalExpr(fn), args)
    }
  /**
   * Evaluate a Lambda <Expr>ession
   * If <expr> is a Symbol, look it up and return it's meaning
   * If it's an Application, evalApp its fn and args
   * If it's a Function, make a new Function with an evaluated body
   * If it's a let, bind a symbol with it's assigned meaning
   * Otherwise, complain.
   */
  def evalExpr(expr:Expr):Expr = { 
    /*
    println("EVAL: " + expr) // for debug! */
    expr match { 
      case Sym(name)      => lookup(name)
      case App(fn, args)  => evalApp(fn, args)
      case Fun(param, body) => Fun(param, evalExpr(body))
      case Let(sym, defn) => { 
	bind(sym.name, defn)
	defn
      }
      case _ => sys error "I don't know what this is: " + expr.toString
    }
  }
  /* Really, this counts the number of 2'nd order recursions in functions with
   * 2 args.  Returns 0 on anything else. */
  def findNumber(expr:Expr):Int = { 
    def numberIter(exp:Expr):Int = exp match { 
      case App(fn,body) => 1 + numberIter(body.head)
      case _ => 0
    }
    expr match { 
      case Fun(param,body) => body match { 
	case Fun(param2, body2) => numberIter(body2)
	case _ => 0
      }
      case _ => 0
    }
  }
  /**
   * This assures that the Lambda Evaluator can be treated as a function
   * These are two overloaded methods, one handles strings, and the other
   * handles already parsed expressions. These return tuples with the result's
   * church number bounded to the result itself.
   */
  def apply(str:String):(Int,Expr) = { 
    eval(parse(str))
  }
  def apply(expr:Expr):(Int,Expr) = { 
    val res = evalExpr(expr)
    (findNumber(res), res)
  }
}

/**
 * This exists just for that smacking-stones-together style testing nobody
 * likes. Feel free to replace it with whatever you want.
 *
object Main {
  def main(args:Array[String]):Unit = {
    println(eval("> succ := ^n^f^x (f (n f x))")._2)
    println(eval("> pred := ^n^f^x (n ^g^h(h (g f)) ^u x ^p p)")._2)
    println(eval("> plus := ^n^m (n succ m)")._2)
    println(eval("> mins := ^m^n (n pred m)")._2)
    println(eval("> mult := ^m^n^f^x (m (n f) x)")._2)
    println(eval("> expt := ^b^e (e (mult b) 1)")._2)
    println(eval("(succ 2)"))
    println(eval("(pred 4)"))
    println(eval("(plus 2 3)"))
    println(eval("(mins 10 5)"))
    println(eval("(mult 3 3)"))
    println(eval("(expt 2 3)"))
  }
} // */
