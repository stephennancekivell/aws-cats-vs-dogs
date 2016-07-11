package catsanddogs

import java.sql.{Connection, DriverManager, ResultSet, Statement}

import ResultSetOptsImplicit._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.Try

object App extends scala.App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val voteFormat = JsonSupport.jsonFormat2(Vote)
    implicit val voteResultFormat = JsonSupport.jsonFormat1(ResultOfVotes)
  }
  import JsonSupport._

  case class ResultOfVotes(votes: Seq[Vote])

  val route =
    path("") {
      get {
        val data = Dao.findResults()
        val rendered = Page.render(data)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, rendered))
      }
    } ~
    path("votes") {
      get {
        val found = Dao.findVotes()

        complete(ResultOfVotes(found))
      }
    } ~
    path("cats") {
      get {
        Dao.voteCats()
        redirect("/", StatusCodes.TemporaryRedirect)
      }
    } ~
    path("dogs") {
      get {
        Dao.voteDogs()
        redirect("/", StatusCodes.TemporaryRedirect)
      }
    } ~
    path("health") {
      get {
        Try(Dao.testConnection()) match {
          case util.Success(true) => complete(200, "ok")
          case util.Success(false) => complete(500, "testConnection = false")
          case util.Failure(tr) => complete(500, tr.getMessage)
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, AppConfig.http.interface, AppConfig.http.port)

  bindingFuture.onComplete { _ =>
    println("listening on "+AppConfig.http.port)
  }

  sys.addShutdownHook(system.terminate())

  Dao.testConnection()
}

case class VotesResult(cats: Option[Int], dogs: Option[Int])

object Dao {
  Class.forName(AppConfig.db.driver)

  val connection = DriverManager.getConnection(AppConfig.db.url, AppConfig.db.user, AppConfig.db.password)

  def voteCats(): Unit = vote("cats")
  def voteDogs(): Unit = vote("dogs")

  private def vote(label: String): Unit = {
    val stmt = connection.createStatement()
    stmt.execute(s"update votes set count = count+1 where label = '$label';")
  }

  def findResults(): VotesResult = {
    val allVotes = findVotes()

    val cats = allVotes.find(_.label == "cats").map(_.count)
    val dogs = allVotes.find(_.label == "dogs").map(_.count)

    VotesResult(cats = cats, dogs = dogs)
  }

  def findVotes(): Seq[Vote] = {
    val stmt = connection.createStatement()

    val rs = stmt.executeQuery("SELECT * FROM votes")
    rs.asAll(VoteReader)
  }

  def testConnection(): Boolean = {
    val stmt = connection.createStatement()

    val result = stmt.executeQuery("SELECT 1;")
    result.next()
    val found = result.getInt(1)
    println("testConnection result "+(found == 1))
    found == 1
  }
}

case class Vote(label: String, count: Int)

object VoteReader extends ResultSetReads[Vote] {
  def read(rs: ResultSet) = Vote(
    label = rs.getString("label"),
    count = rs.getInt("count")
  )
}
