package com.github.wesleybits

import java.awt.Font
import processing.core._
import processing.core.PConstants._

object consts {
  val W = 900
  val H = 600
}

/* Lambda UI is exceedingly imperative, simply becuase
 * imperative programming handles UI nicely when
 * Processing is involved.
 */
class LambdaUI extends PApplet { 
  import PApplet._
  
  var Lfont:PFont = new PFont(new Font("courier", Font.PLAIN, 42), true)
  
  object memory {
    var mem:List[String] = List()
    var newEntry = ""
    var pos = (5, 5)
    var status = 0
    def add(str:String):Unit = { newEntry = str; status = -5 }
    def draw:Unit = {
      if ((pos._2 < -20) && (status < 0)) {
	status = 5
	mem = newEntry :: mem
      }
      else if ((pos._2 == 15) && (status > 0)) status = 0
      else { 
	pos = (pos._1, pos._2 + status)
	backpic.thad += 0.0005f + status / 100f
      }
      fill(255,0,0,200)
      textFont(Lfont,12)
      pushMatrix
	translate(0, height)
	rotate(- PI/2)
	drawStringList(mem, pos._1, pos._2, 13)
      popMatrix
    }
  }
  object input {
    var field = ":"
    var pos = (20,20)
    var status = 0
    def append(ch:Char):Unit = field = field + ch
    def draw:Unit = {
      if ((pos._2 < -100) && (status < 0)) {
	status = 0
	pos = (20,20)
	field = ":"
      } else pos = (pos._1, pos._2 + status)
      fill(0)
      textFont(Lfont,18)
      text(field, pos._1, pos._2)
    }
  }
  object backpic {
    var thad = 0.0f
    def drawLayer(layers:Int, range:Int, prom:Int, thad:Float):Unit = {
      val r = (prom / (layers * 0.95f))
      pushMatrix
      translate((3 * width) / 4, (3 * height) / 4)
      rotate(thad)
      fill(30 * (sin(2 * thad) + 1),0,60 * (sin(4 * thad) + 1), 100)
      val limit = 2 * PI
      val step = PI / 8
      for ( i <- 0f until limit by step ) {
	rotate(i)
	ellipse(-range * cos(2 * thad) - prom, 0, r, r)
      }
      popMatrix
    }
    def draw:Unit = {
      val layers = 7
      val limit = height / 2
      val step = limit / layers
      var th = thad
      for ( i <- 0 to limit by step ) {
	drawLayer(layers, 20, i, th)
	th = th * -1f
      }
    }
  }

  private def drawStringList(strlst:List[String], x:Int, y:Int, size:Int):Unit = {
    for ( i <- 0 to (strlst.length - 1) ) {
      text(strlst(i), x, y + (i * size)) 
    } 
  }

  override def setup:Unit = { 
    size(consts.W, consts.H)
    background(255)
    noStroke
    smooth
  }
  override def draw:Unit = {
    background(255)
    backpic.draw
    memory.draw
    input.draw
  }
  override def keyTyped():Unit = {
    if (key == '\n') {
      try {
	memory.add(eval(input.field.tail).toString)
      } catch {
	case e:Exception => 
	  memory.add(e.toString)
      }
      input.status = -15
    } else if (key == '\b') {
      input.field = input.field.slice(0, input.field.length - 1)
    } else input.append(key)
  }
}

object Main { 
  import java.awt.{ Frame, Panel }
  import java.awt.event.{ WindowAdapter, WindowEvent }

  def main(args:Array[String]):Unit = {
    val panel = new Panel
    val frame = new Frame("LambdaFall")
    val sketch = new LambdaUI
    
    frame add sketch
    frame setSize (consts.W, consts.H + 20)
    frame addWindowListener (new WindowAdapter { 
      override def windowClosing(e:WindowEvent) = { 
	frame.dispose
	sketch.dispose
	sys exit 0
      }
    })
    frame setVisible true
    sketch.init
  }
}
