package controllers

import dao.{NeologismDAO}

import javax.inject.Inject
import models.{NeologismSubmission}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import play.api.data.validation.Constraints.{max, min}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}



object SubmissionErrors {
  val errorStrings = Array(
    InvalidString -> "Neologism must contain at least four characters, not exceed 32 characters, and lack whitespace or numbers or punctuation.",
    AlreadyFound -> "Neologism already found in the corpus of all English.",
    MissingAttribution -> "Neologism submission lacks attribution.",
    MissingExplanation -> "Neologism submission lacks explanation.",
    AttributionTooLong -> "Attribution must be less than 64 characters, and only contain whitespace or alphanumeric characters",
    ExplanationTooLong -> "Explanation must be less than 100 characters, and only contain whitespace or alphanumeric characters",
  )
}

enum SubmissionErrors {
  case InvalidString, AlreadyFound, MissingAttribution, MissingExplanation, AttributionTooLong, ExplanationTooLong
}


class Application @Inject() (
    neologismDao: NeologismDAO,
    mcc: MessagesControllerComponents,
    lifecycle: ApplicationLifecycle,
    startup: ApplicationStart
)(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {


  lifecycle.addStopHook { () =>
    Future.successful {
      println("DB stopped.")
      startup.server.stop()
    }
  }
  /*
    def mapping[R, A1, A2](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]))(apply: Function2[A1, A2, R])(unapply: Function1[R, Option[(A1, A2)]]): Mapping[R] = {
    new ObjectMapping2(apply, unapply, a1, a2)
  }

Params:
apply – A function able to create a value of R from a value of A1 (If R is case class you can use its own apply function)
unapply – A function able to create A1 from a value of R (If R is a case class you can use its own unapply function)
Type parameters:
R – the mapped type
Returns:
a mapping for type R

   */

  val neologismForm: Form[NeologismSubmission] = Form(
    mapping[NeologismSubmission, String, String, String](
      "quotation" -> text(),
      "attribution" -> text(),
      "explanation" -> text(),
    )
    (NeologismSubmission.makeWithCurrentTime)
    (NeologismSubmission.getFormComponents)
  )


  val textPath = "./words_alpha.txt"

  // run markov counts
  println("getting counts")
  val counts = Markov.GetLetterCounts(textPath)

  println("getting probabilities")
  val probabilities = Markov.CalculateProbabilities(counts)

  val digramCounts = Markov.GetDigramCounts(textPath)
  val digramProbabilities = Markov.CalculateDigramProbabilities(digramCounts)

  val oneGrams = Markov.GetNgramCounts(textPath, 1)
  val twoGrams = Markov.GetNgramCounts(textPath, 2)
  val threeGrams = Markov.GetNgramCounts(textPath, 3)
  val fourGrams = Markov.GetNgramCounts(textPath, 4)


  def index(submissionID: Option[Long], error: Option[Int]) = Action.async { implicit request =>

    val allFuture = neologismDao.all()
    val selectedFuture = neologismDao.lookupByID(submissionID)

    for {
      neologisms <- allFuture
      selected <- selectedFuture
    } yield {
      val errorText = error.map(SubmissionErrors.errorStrings(_)._2)
      Ok(views.html.index(neologismForm, neologisms, selected, errorText))
    }

  }




  def insertNeologism = Action.async { implicit request =>
    val submission: NeologismSubmission = neologismForm.bindFromRequest().get

    val error = submission.validate.map(_.ordinal)

    if (error.nonEmpty) {
      Future(Redirect(routes.Application.index(None, error)))
    } else {
      neologismDao.lookupOrInsert(submission).map { found =>
        Redirect(routes.Application.index(found, error))
      }
    }
  }

}
