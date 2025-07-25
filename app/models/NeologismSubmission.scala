package models

import controllers.SubmissionErrors
import models.NeologismSubmission._

import java.sql.Timestamp

object NeologismSubmission {

  def ValidateQuotation(quote: String): Boolean = {
    quote.matches("^[a-zA-Z]{4,32}$")
  }

  def ValidateAttribution(quote: String): Boolean = {
    quote.matches("^[a-zA-Z0-9 ]{1,64}$")
  }

  def ValidateExplanation(quote: String): Boolean = {
    quote.matches("^[a-zA-Z0-9 &$!()?.,;'-]{4,100}$")
  }

  def makeWithCurrentTime(quotation: String, attribution: String, explanation: String): NeologismSubmission = {
    NeologismSubmission(quotation, attribution, explanation, Timestamp.from(java.time.Instant.now()))
  }

  def getFormComponents(neologismSubmission: NeologismSubmission): Option[(String, String, String)] = {
    Some(neologismSubmission.quotation, neologismSubmission.attribution, neologismSubmission.explanation)
  }

  def unapply(submission: NeologismSubmission): Option[(String, String, String, Timestamp)] = Some((submission.quotation, submission.attribution, submission.explanation, submission.timestamp))

  def tupled = (this.apply _).tupled

}


case class NeologismSubmission(quotation: String, attribution: String, explanation : String, timestamp: Timestamp) {

  def validate : Option[SubmissionErrors] = {
    if(!ValidateQuotation(quotation)) Some(SubmissionErrors.InvalidString)
    else if (!ValidateAttribution(attribution)) Some(SubmissionErrors.AttributionTooLong)
    else if (!ValidateExplanation(attribution)) Some(SubmissionErrors.ExplanationTooLong)
    else None
  }

}

