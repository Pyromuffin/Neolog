package controllers

import org.h2.tools.Server

import javax.inject.*
import scala.concurrent.Future
import play.api.inject.ApplicationLifecycle
import com.google.inject.{AbstractModule, Provides}


@Singleton
class ApplicationStart  {

  val config = Server.createTcpServer(
    //"-tcpAllowOthers", // Allow connections from other machines
    "-tcpPort", "9069", // Specify the port (default is 9092)
    "-baseDir", ".", // Specify a base directory for database files
  )

  println("Database started.")
  val server = config.start()

}

// this dance is strange because as eager singleton just doesn't seem to work?
val start = new ApplicationStart()

class StartModule extends AbstractModule {
  override def configure() = {
    bind(classOf[ApplicationStart]).toInstance(start)
  }
}
