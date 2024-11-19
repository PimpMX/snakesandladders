package snakes
package util

import scala.swing.{Publisher, event}

trait Observer {
  def update(e: Event): Unit
}

trait Observable extends Publisher {
  var subscribers: Vector[Observer] = Vector()
  def add(s: Observer) = subscribers = subscribers :+ s
  def notifyObservers(e: Event) = {
    subscribers.foreach(o => o.update(e))
    publish(e)
  }
}

enum Event extends event.Event {
  case Create
  case AddPlayer
  case Roll(rollResult: Int)
  case Undo
  case Start
  case Load
  case Save
  case Update
  case Restart
}