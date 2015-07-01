
/* run in sbt: run sample.kernel.hello.HelloKernelBootable */


package sample.kernel.hello

import akka.actor.{ Actor, ActorSystem, Props }
import akka.kernel.Bootable
import java.lang.Boolean.getBoolean
import java.net.URLClassLoader
import java.util.jar.JarFile
import scala.collection.immutable
import scala.collection.JavaConverters._
import java.io.File

case object Start


class HelloActor extends Actor {
  val worldActor = context.actorOf(Props[WorldActor])

  def receive = {
    case Start => worldActor ! "Hello"
    case message: String =>
      println("Received message '%s'" format message)
  }
}

class WorldActor extends Actor {
  def receive = {
    case message: String => sender ! (message.toUpperCase + " world!")
  }
}

class HelloKernelBootable extends Bootable {
  val system = ActorSystem("hellokernel")

  def startup = {
    system.actorOf(Props[HelloActor]) ! Start
  }

  def shutdown = {
    system.shutdown()
  }
}

object HelloKernel {

  private val quiet = getBoolean("akka.kernel.quiet")

  private def log(s: String) = if (!quiet) println(s)

  @deprecated("Microkernel is deprecated. Use ordinary main class instead.", "2.4")
  def main(args: Array[String]) = {

    if (args.isEmpty) {
      log("[error] No boot classes specified")
      System.exit(1)
    }

    log("Running Akka " + ActorSystem.Version)

    val classLoader = createClassLoader()

    Thread.currentThread.setContextClassLoader(classLoader)

    val bootClasses: immutable.Seq[String] = args.to[immutable.Seq]
    val bootables: immutable.Seq[Bootable] = bootClasses map { c ⇒ classLoader.loadClass(c).newInstance.asInstanceOf[Bootable] }

    for (bootable ← bootables) {
      log("Starting up " + bootable.getClass.getName)
      bootable.startup()
    }

    addShutdownHook(bootables)

    log("Successfully started Akka")
  }

  private def loadDeployJars(deploy: File): ClassLoader = {
    val jars = deploy.listFiles.filter(_.getName.endsWith(".jar"))

    val nestedJars = jars flatMap { jar ⇒
      val jarFile = new JarFile(jar)
      val jarEntries = jarFile.entries.asScala.toArray.filter(_.getName.endsWith(".jar"))
      jarEntries map { entry ⇒ new File("jar:file:%s!/%s" format (jarFile.getName, entry.getName)) }
    }

    val urls = (jars ++ nestedJars) map { _.toURI.toURL }

    urls foreach { url ⇒ log("Deploying " + url) }

    new URLClassLoader(urls, Thread.currentThread.getContextClassLoader)
  }

  private def createClassLoader(): ClassLoader = {
    if (ActorSystem.GlobalHome.isDefined) {
      val home = ActorSystem.GlobalHome.get
      val deploy = new File(home, "deploy")
      if (deploy.exists) {
        loadDeployJars(deploy)
      } else {
        log("[warning] No deploy dir found at " + deploy)
        Thread.currentThread.getContextClassLoader
      }
    } else {
      log("[warning] Akka home is not defined")
      Thread.currentThread.getContextClassLoader
    }
  }


  private def addShutdownHook(bootables: immutable.Seq[Bootable]): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run = {
        log("")
        log("Shutting down Akka...")

        for (bootable ← bootables) {
          log("Shutting down " + bootable.getClass.getName)
          bootable.shutdown()
        }

        log("Successfully shut down Akka")
      }
    }))
  }
}