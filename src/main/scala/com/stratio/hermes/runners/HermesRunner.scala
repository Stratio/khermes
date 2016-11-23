package com.stratio.hermes.runners

/**
 * Entry point of the application.
 */
object HermesRunner extends App {
  println(helloWorld("Hermes"))

  def helloWorld(projectName: String): String = s"Hello $projectName!"
}
