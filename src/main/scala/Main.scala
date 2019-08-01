import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object Main {

  implicit val connectionAskCloseDuration: FiniteDuration = 15 second
  val awaitMongoShitSuicideDuration: FiniteDuration = 15 second
  val bdName = "firstdb"

  val mongoUri = s"mongodb://localhost:27017/$bdName"

  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver = MongoDriver()

  val database: Future[DefaultDB] = for {
    url <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    con = driver.connection(url, strictUri = true).getOrElse(throw new IllegalStateException("Юра, бд пошла по пизде"))
    dn  = url.db.getOrElse(throw new IllegalArgumentException("Юра проебал юрл"))
    db  <- con.database(dn)
  } yield db

  val document1 = BSONDocument(
    "firstName" -> "Stephane",
    "lastName"  -> "Godbillon",
    "age"       -> 29)

  // Simple: .insert.one(t)
  def simpleInsert(coll: BSONCollection): Future[WriteResult] =
    coll
      .insert
      .one(document1)
      .map { wr =>
        println(s"successfully inserted document with result: $wr")
        wr
      }

  def killMongo: Future[Unit] =  for {
    db <- database
    _ <- db.connection.askClose()
    _ = driver.close()
  } yield ()

  def main(args: Array[String]): Unit = {
    val personCollection = database.map(_.collection("person"))
    Await
      .result(
        personCollection
          .flatMap(simpleInsert)
          .flatMap(_ => killMongo),
        awaitMongoShitSuicideDuration
      )
  }
}

// Custom persistent types
case class Person(firstName: String, lastName: String, age: Int)