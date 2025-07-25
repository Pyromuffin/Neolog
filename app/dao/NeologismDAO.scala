package dao

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import models.{NeologismSubmission}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.ZonedDateTime

class NeologismDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Submissions = TableQuery[NeologismTable]

  def all(): Future[Seq[NeologismSubmission]] = db.run(Submissions.result)

  def insert(submission: NeologismSubmission): Future[Unit] = db.run(Submissions += submission).map { _ => () }

  def lookupByID(id : Option[Long]) : Future[Option[NeologismSubmission]] = {

    if(id.nonEmpty){
      db.run {
        Submissions.filter(_.id === id).take(1).result.headOption
      }
    } else {
      Future.successful(None)
    }
  }

  def lookupOrInsert(submission: NeologismSubmission): Future[Option[Long]] = {
    db.run {
      Submissions.filter(_.quotation === submission.quotation).take(1).map(_.id).result.headOption.flatMap{
        case Some(dbItem) =>
          DBIO.successful(Some(dbItem))
        case None =>
          (Submissions += submission).map(_ => None)
      }
    }
  }
  private class NeologismTable(tag: Tag) extends Table[NeologismSubmission](tag, "NEOLOGISM") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def quotation = column[String]("QUOTATION")
    def attribution = column[String]("ATTRIBUTION")
    def timestamp = column[Timestamp]("TIME")
    def explanation = column[String]("EXPLANATION")
    def rarity = column[Int]("RARITY")
    def tonitrus = column[Float]("TONITRUS")

    def * = (quotation, attribution, explanation, timestamp) <> (NeologismSubmission.tupled, NeologismSubmission.unapply)
  }
}