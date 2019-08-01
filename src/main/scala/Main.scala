import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object Main {
  val mongoUri = "mongodb://localhost:27017/firstdb"

  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver = MongoDriver()

  val database: Future[DefaultDB] = for {
    url <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    con = driver.connection(url)
    dn <- Future(url.db.get)
    db <- con.database(dn)
  } yield db

  val document1 = BSONDocument(
    "firstName" -> "Stephane",
    "lastName" -> "Godbillon",
    "age" -> 29)

  // Simple: .insert.one(t)
  def simpleInsert(coll: BSONCollection): Future[Unit] = {
    val writeRes: Future[WriteResult] = coll.insert.one(document1)

    writeRes.onComplete { // Dummy callbacks
      case Failure(e) => e.printStackTrace()
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }

    writeRes.map(_ => {}) // in this example, do nothing with the success
  }

  def main(args: Array[String]): Unit = {
    val personCollection = database.map(_.collection("person"))
    val res = personCollection.map(simpleInsert(_))
    Await.ready(res, 60.seconds)
    driver.close()
  }
}

// Custom persistent types
case class Person(firstName: String, lastName: String, age: Int)
