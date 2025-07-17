package controllers

import dao.{CatDAO, DogDAO, NeologismDAO}

import javax.inject.Inject
import models.{Cat, Dog, NeologismSubmission}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.validation.Constraints.{max, min}

import scala.concurrent.{ExecutionContext, Future}

class Application @Inject() (
    catDao: CatDAO,
    dogDao: DogDAO,
    neologismDao: NeologismDAO,
    mcc: MessagesControllerComponents
)(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {

  val catForm: Form[Cat] = Form(
    mapping(
      "name" -> text(minLength = 10),
      "color" -> text(),
      "cuteness" -> number()
    )(Cat.apply)(Cat.unapply)
  )

  val dogForm: Form[Dog] = Form(
    mapping(
      "name" -> text(),
      "color" -> text()
    )(Dog.apply)(Dog.unapply)
  )


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
    mapping[NeologismSubmission, String, String](
      "quotation" -> text(),
      "attribution" -> text(),
    )
    (NeologismSubmission.makeWithCurrentTime)
    (NeologismSubmission.getFormComponents)
  )





  // run markov counts
  println("getting counts")
  val counts = Markov.GetLetterCounts("/home/kelly/Downloads/words_alpha.txt")

  println("getting probabilities")
  val probabilities = Markov.CalculateProbabilities(counts)

  val digramCounts = Markov.GetDigramCounts("/home/kelly/Downloads/words_alpha.txt")
  val digramProbabilities = Markov.CalculateDigramProbabilities(digramCounts)

  val oneGrams = Markov.GetNgramCounts("/home/kelly/Downloads/words_alpha.txt", 1)
  val twoGrams = Markov.GetNgramCounts("/home/kelly/Downloads/words_alpha.txt", 2)
  val threeGrams = Markov.GetNgramCounts("/home/kelly/Downloads/words_alpha.txt", 3)
  val fourGrams = Markov.GetNgramCounts("/home/kelly/Downloads/words_alpha.txt", 4)
  //Markov.GetWordStatistics("/home/kelly/Downloads/words_alpha.txt")


  def index(submissionID: Option[Long]) = Action.async { implicit request =>

    val allFuture = neologismDao.all()
    val selectedFuture = neologismDao.lookupByID(submissionID)

    for {
      neologisms <- allFuture
      selected <- selectedFuture
    } yield {
      Ok(views.html.index(neologismForm, neologisms, selected))
    }

  }


  def insertNeologism = Action.async { implicit request =>
    val submission: NeologismSubmission = neologismForm.bindFromRequest().get

    val result = neologismDao.lookupOrInsert(submission).map{ found =>
        Redirect(routes.Application.index(found))
    }
    result
  }

}
