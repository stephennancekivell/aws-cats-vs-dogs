package catsanddogs

import com.typesafe.config.ConfigFactory

import scala.util.Try

object AppConfig {

  private val config = ConfigFactory.load

  object db {
    val url = config.getString("db.url")
    val driver = config.getString("db.driver")
    val user = config.getString("db.user")
    val password = config.getString("db.password")
  }

  object http {
    val port = config.getInt("http.port")
    val interface = config.getString("http.interface")
  }

  val hostname = Try(config.getString("hostname")).toOption
}
