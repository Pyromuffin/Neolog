package models

import play.api.mvc.QueryStringBindable

import java.sql.Timestamp
import java.time.ZonedDateTime


case class Cat(name: String, color: String, cuteness: Int)
object Cat {
  def unapply(cat: Cat): Option[(String, String, Int)] = Some((cat.name, cat.color, cat.cuteness))
  def tupled = (this.apply _).tupled
}

case class Dog(name: String, color: String)
object Dog {
  def unapply(dog: Dog): Option[(String, String)] = Some((dog.name, dog.color))
  def tupled = (this.apply _).tupled
}


case class NeologismSubmission(quotation: String, attribution: String, timestamp: Timestamp)


object NeologismSubmission {
  def makeWithCurrentTime(quotation: String, attribution: String) : NeologismSubmission = {
    NeologismSubmission(quotation, attribution, Timestamp.from(java.time.Instant.now() ))
  }

  def getFormComponents(neologismSubmission: NeologismSubmission) : Option[(String, String)] = {
    Some(neologismSubmission.quotation, neologismSubmission.attribution)
  }

  def unapply(submission: NeologismSubmission): Option[(String, String, Timestamp)] = Some((submission.quotation, submission.attribution, submission.timestamp))
  def tupled = (this.apply _).tupled

}