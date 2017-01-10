/*
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.hermes.helpers

import java.io.File
import java.lang.reflect.Method
import java.net._

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.utils.HermesLogging
import com.typesafe.config.Config
import play.twirl.compiler.{GeneratedSource, TwirlCompiler}

import scala.collection.mutable
import scala.reflect.internal.util.Position
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.{Global, Settings}

/**
 * Helper used to parse and compile templates using Twirl.
 */
object TwirlHelper extends HermesLogging {

  /**
   * Compiles and executes a template with Twirl. It follows the next steps:
   * Step 1) A string that represents the template is saved in Hermes' templates path.
   * Step 2) The engine generates a scala files to be compiled.
   * Step 3) The engine compiles the scala files generated in the previous step.
   * Step 4) Finally it executes the compiled files interpolating values with the template.
   * @param template a string with the template.
   * @param templateName the name of the file that will contain the content of the template.
   * @param config with Hermes' configuration.
   * @tparam T with the type of object to inject in the template.
   * @return a compiled and executed template.
   */
  def template[T](template: String, templateName: String)(implicit config: Config): CompiledTemplate[T] = {
    val templatesPath = config.getString("hermes.templates-path")
    val templatePath = s"$templatesPath/$templateName.scala.html"
    scala.tools.nsc.io.File(templatePath).writeAll(template)

    val sourceDir = new File(templatesPath)
    val generatedDir = new File(s"$templatesPath/${HermesConstants.ConstantGeneratedTemplatesPrefix}")
    val generatedClasses = new File(s"$templatesPath/${HermesConstants.ConstantGeneratedClassesPrefix}")

    deleteRecursively(generatedDir)
    deleteRecursively(generatedClasses)
    generatedClasses.mkdirs()

    val helper = new CompilerHelper(sourceDir, generatedDir, generatedClasses)
    helper.compile[T](s"$templateName.scala.html", s"html.$templateName", Seq("com.stratio.hermes.utils.Hermes"))
  }

  /**
   * If the template is wrong this exception informs about the mistake.
   * @param message with information about the error.
   * @param line that contains the error.
   * @param column that contains the error.
   */
  case class CompilationError(message: String, line: Int, column: Int) extends RuntimeException(message)

  /**
   * Deletes all content in a path.
   * @param dir a file that represents the path to delete.
   */
  protected[this] def deleteRecursively(dir: File) {
    if(dir.isDirectory) dir.listFiles().foreach(deleteRecursively)
    dir.delete()
  }

  /**
   * Helper used to compile templates internally.
   * @param sourceDir that contains original templates.
   * @param generatedDir that contains scala files from the templates.
   * @param generatedClasses that contains class files with the result of the compilation.
   */
  protected[this] class CompilerHelper(sourceDir: File, generatedDir: File, generatedClasses: File) {

    val twirlCompiler = TwirlCompiler
    val classloader = new URLClassLoader(Array(generatedClasses.toURI.toURL),
      Class.forName("play.twirl.compiler.TwirlCompiler").getClassLoader)
    val compileErrors = new mutable.ListBuffer[CompilationError]

    val compiler = {
      def additionalClassPathEntry: Option[String] = Some(
        Class.forName("play.twirl.compiler.TwirlCompiler")
          .getClassLoader.asInstanceOf[URLClassLoader]
          .getURLs.map(url => new File(url.toURI)).mkString(":"))

      val settings = new Settings
      val scalaObjectSource = Class.forName("scala.Option").getProtectionDomain.getCodeSource

      val compilerPath = Class.forName("scala.tools.nsc.Interpreter").getProtectionDomain.getCodeSource.getLocation
      val libPath = scalaObjectSource.getLocation
      val pathList = List(compilerPath, libPath)
      val originalBootClasspath = settings.bootclasspath.value
      settings.bootclasspath.value =
        ((originalBootClasspath :: pathList) ::: additionalClassPathEntry.toList) mkString File.pathSeparator
      settings.outdir.value = generatedClasses.getAbsolutePath

      new Global(settings, new ConsoleReporter(settings) {
        override def printMessage(pos: Position, msg: String) = {
          compileErrors.append(CompilationError(msg, pos.line, pos.point))
        }
      })
    }

    def compile[T](templateName: String, className: String, additionalImports: Seq[String] = Nil): CompiledTemplate[T] = {
      val templateFile = new File(sourceDir, templateName)
      val Some(generated) = twirlCompiler.compile(templateFile, sourceDir, generatedDir, "play.twirl.api.TxtFormat",
        additionalImports = TwirlCompiler.DefaultImports ++ additionalImports)
      val mapper = GeneratedSource(generated)
      val run = new compiler.Run
      compileErrors.clear()
      run.compile(List(generated.getAbsolutePath))

      compileErrors.headOption.foreach {
        case CompilationError(msg, line, column) =>
          compileErrors.clear()
          throw CompilationError(msg, mapper.mapLine(line), mapper.mapPosition(column))
      }
      new CompiledTemplate[T](className, classloader)
    }
  }

  /**
   * From a classname and a classloader it returns a result of a compiled and executed template.
   * @param className with the classname.
   * @param classloader with the classloader.
   * @tparam T with the type of object to inject in the template.
   */
  class CompiledTemplate[T](className: String, classloader: URLClassLoader) {
    var method: Option[Method] = None
    var declaredField: Option[AnyRef] = None

    private def getF(template: Any) = {
      if(method.isEmpty) {
        method = Option(template.getClass.getMethod("f"))
        method.get.invoke(template).asInstanceOf[T]
      } else {
        method.get.invoke(template).asInstanceOf[T]
      }
    }
    /**
     * @return the result of a compiled and executed template.
     */
    //scalastyle:off
    def static: T = {
      if(declaredField.isEmpty) {
        declaredField = Option(classloader.loadClass(className + "$").getDeclaredField("MODULE$").get(null))
        getF(declaredField.get)
      } else {
        getF(declaredField.get)
      }
    }
    //scalastyle:on
  }
}
