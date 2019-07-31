import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main {
  val mongoUri = "mongodb://localhost:27017/mydb?authMode=scram-sha1"

  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver = MongoDriver()
  val parsedUri = MongoConnection.parseURI(mongoUri)
  val connection = parsedUri.map(driver.connection(_))

  // Database and collections: Get references
  val futureConnection = Future.fromTry(connection)

  def db1: Future[DefaultDB] = futureConnection.flatMap(_.database("firstdb"))
  def db2: Future[DefaultDB] = futureConnection.flatMap(_.database("anotherdb"))
  def personCollection: Future[BSONCollection] = db1.map(_.collection("person"))

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
    personCollection.onComplete {
      case Failure(e) => e.printStackTrace()
      case Success(collection) => simpleInsert(collection)
    }
  }
}

// Custom persistent types
case class Person(firstName: String, lastName: String, age: Int)
