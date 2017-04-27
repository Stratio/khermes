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

package com.stratio.khermes.helpers.twirl

import java.io._
import java.lang.reflect.Method
import java.net._

import com.stratio.khermes.commons.constants.AppConstants
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import play.twirl.compiler.TwirlCompiler.TemplateAsFunctionCompiler.PresentationCompiler.global
import play.twirl.compiler.TwirlCompiler.TemplateAsFunctionCompiler._
import play.twirl.compiler.TwirlCompiler._
import play.twirl.compiler._
import play.twirl.parser.TreeNodes.{Constructor, Def, PosString, Template}
import play.twirl.parser.TwirlParser

import scala.collection.mutable
import scala.io.Codec
import scala.reflect.internal.util.{BatchSourceFile, Position, SourceFile}
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.{Global, Settings}

/**
 * Helper used to parse and compile templates using Twirl.
 */
object TwirlHelper extends LazyLogging {

  /**
   * Compiles and executes a template with Twirl. It follows the next steps:
   * Step 1) A string that represents the template is saved in Khermes' templates path.
   * Step 2) The engine generates a scala files to be compiled.
   * Step 3) The engine compiles the scala files generated in the previous step.F
   * Step 4) Finally it executes the compiled files interpolating values with the template.
   * @param template a string with the template.
   * @param templateName the name of the file that will contain the content of the template.
   * @param config with Khermes' configuration.
   * @tparam T with the type of object to inject in the template.
   * @return a compiled and executed template.
   */
  def template[T](template: String, templateName: String)(implicit config: Config): Option[CompiledTemplate[T]] = {
    val templatesPath = config.getString("khermes.templates-path")
    val templatePath = s"$templatesPath/$templateName.scala.html"
    scala.tools.nsc.io.File(templatePath).writeAll(template)

    val sourceDir = new File(templatesPath)
    val generatedDir = new File(s"$templatesPath/${AppConstants.GeneratedTemplatesPrefix}")
    val generatedClasses = new File(s"$templatesPath/${AppConstants.GeneratedClassesPrefix}")

    deleteRecursively(generatedDir)
    deleteRecursively(generatedClasses)
    generatedClasses.mkdirs()
    generatedDir.mkdirs()

    val helper = new CompilerHelper(sourceDir, generatedDir, generatedClasses)
    helper.compile[T](s"$templateName.scala.html", s"html.$templateName", Seq("com.stratio.khermes.helpers.faker.Faker"))
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


    def myCompilation(source: File, sourceDirectory: File, generatedDirectory: File, formatterType: String,
                additionalImports: Seq[String] = Nil, constructorAnnotations: Seq[String] = Nil, codec: Codec = TwirlIO2.defaultCodec,
                inclusiveDot: Boolean = false) = {
      val resultType = formatterType + ".Appendable"
      val (templateName, generatedSource) = generatedFile(source, codec, sourceDirectory, generatedDirectory, inclusiveDot)
      if (generatedSource.needRecompilation(additionalImports)) {
        try {
          val generated = myparseAndGenerateCode(templateName, TwirlIO2.readFile(source), codec, source.getAbsolutePath,
            resultType, formatterType, additionalImports, constructorAnnotations, inclusiveDot)
          logger.debug("********* ABOUT TO WRITE STRING TO FILE ***********")
          Thread.sleep(10000)
          TwirlIO2.writeStringToFile(generatedSource.file, generated.toString, codec)
          logger.debug("********* WRITE FINE!!!!!! ***********")
          Thread.sleep(10000)
          Some(generatedSource.file)
        } catch {
          case TemplateCompilationError(source, message, line, column) => {
            logger.error(s"ERROR!!! SOURCE $source MESSAGE -> $message LINE -> $line COLUMN -> $column")
            None
          }
          case _ => {
            logger.error("Otra excepción joder!!!!!")
            None
          }
        }
      } else {
        None
      }
    }

    def mytemplateCode(template: Template, resultType: String): Seq[Any] = {
      logger.debug("********* MY TEMPLATE CODE ***********")
      Thread.sleep(5000)
      val defs = (template.sub ++ template.defs).map { i =>
        i match {
          case t: Template if t.name == "" => templateCode(t, resultType)
          case t: Template => {
            Nil :+ (if (t.name.str.startsWith("implicit")) "implicit def " else "def ") :+ Source(t.name.str, t.name.pos) :+ Source(t.params.str, t.params.pos) :+ ":" :+ resultType :+ " = {_display_(" :+ templateCode(t, resultType) :+ ")};"
          }
          case Def(name, params, block) => {
            Nil :+ (if (name.str.startsWith("implicit")) "implicit def " else "def ") :+ Source(name.str, name.pos) :+ Source(params.str, params.pos) :+ " = {" :+ block.code :+ "};"
          }
        }
      }

      val imports = formatImports(template.imports)
      logger.debug("********* IMPORTS ***********"+imports)
      Thread.sleep(5000)
      Nil :+ imports :+ "\n" :+ defs :+ "\n" :+ "Seq[Any](" :+ visit(template.content, Nil) :+ ")"
    }


    def mygetFunctionMapping(signature: String, returnType: String): (String, String, String) = synchronized {
      logger.debug(s"********* MY GET FUNCTION MAPPING. SIGNATURE $signature AND RETURNTYPE $returnType ***********")
      Thread.sleep(5000)
      def filterType(t: String) = t
        .replace("_root_.scala.<repeated>", "Array")
        .replace("<synthetic>", "")

      def findSignature(tree: Tree): Option[DefDef] = {
        tree match {
          case t: DefDef if t.name.toString == "signature" => {
            logger.debug("SIGNATURE SOMEEEEE!!!!!!!!!!!!!1")
            Thread.sleep(5000)
            Some(t)
          }
          case t: Tree => {
            logger.debug("SIGNATURE FLATMAP!!!!!")
            Thread.sleep(5000)
            logger.debug("T.CHILDREN --> "+t.children.size)
            t.children.flatMap(findSignature).headOption
          }
        }
      }

      def myTreeFrom(src: String): global.Tree = {
        logger.debug(s"********* MY TREE FROM WITH FILE ----> $src")
        Thread.sleep(5000)
        val randomFileName = {
          val r = new java.util.Random
          () => "file" + r.nextInt
        }
        logger.debug("RANDOM FILE NAME -->"+randomFileName)
        Thread.sleep(5000)
        val file = new BatchSourceFile(randomFileName(), src)
        logger.debug("CALLING... PresentationCompiler.treeFrom(file)")
        Thread.sleep(5000)
        try {
          treeFrom2(file)
          logger.debug("PresentationCompiler.treeFrom(file) OK!!!!!!!!!!!!!")
        } catch {
          case ex: Throwable => logger.error("EXCEPTION CABROOONNNN!!!!!!"+ex.getMessage+" STACK TRACE --->>"+ex.printStackTrace())
        }

        PresentationCompiler.treeFrom(file)
      }

      def treeFrom2(file: SourceFile): global.Tree = {
        logger.debug("TREEFROM2!!!!!")
        Thread.sleep(5000)
        import tools.nsc.interactive.Response
        val r1 = new Response[global.Tree]
        logger.debug("ABOUT TO ASK PARSED ENTERED THE FOLLOWING FILE: ")
        logger.debug("FILE PATH: "+file.path+" FILE CONTENT: "+file.toString()+" FILE: "+file.content)
        Thread.sleep(10000)
        //PETAAA AQUIIIIIIIIIIIIIIIIIIIIIIIIIIIII
        global.askParsedEntered(file, true, r1)
        logger.debug("ASK PARSED ENTERED FINE!!!!!!!!....")
        Thread.sleep(10000)
        r1.get match {
          case Left(result) =>
            result
          case Right(error) =>
            throw error
        }
      }

      val treeFrom = myTreeFrom("object FT { def signature" + signature + " }")

      logger.debug(s"********* TREEFORM ID ----> ${treeFrom.id}")
      Thread.sleep(5000)

      Thread.sleep(5000)
      val params = findSignature(
        PresentationCompiler.treeFrom("object FT { def signature" + signature + " }")).get.vparamss

      logger.debug("********* FIND SIGNATURE OK!!!!!!!!!!!***********")
      Thread.sleep(5000)

      val resp = PresentationCompiler.global.askForResponse { () =>

        val functionType = "(" + params.map(group => "(" + group.map {
          case ByNameParam(_, paramType) => " => " + paramType
          case a => filterType(a.tpt.toString)
        }.mkString(",") + ")").mkString(" => ") + " => " + returnType + ")"

        val renderCall = {
          logger.debug("********* RENDER CALL!!!!!!!!!!!***********")
          Thread.sleep(5000)
          val call = "def render%s: %s = apply%s".format(
            "(" + params.flatten.map {
              case ByNameParam(name, paramType) => name + ":" + paramType
              case a => a.name.toString + ":" + filterType(a.tpt.toString)
            }.mkString(",") + ")",
            returnType,
            params.map(group => "(" + group.map { p =>
              p.name.toString + Option(p.tpt.toString).filter(_.startsWith("_root_.scala.<repeated>")).map(_ => ":_*").getOrElse("")
            }.mkString(",") + ")").mkString)
          logger.debug("********* RENDER CALL OK!!!!!!!!!!!***********")
          Thread.sleep(5000)
          call
        }

        val templateType = {
          logger.debug("********* TEMPLATE TYPE!!!!!!!!!!!***********")
          Thread.sleep(5000)
          val root = "_root_.play.twirl.api.Template%s[%s%s]".format(
            params.flatten.size,
            params.flatten.map {
              case ByNameParam(_, paramType) => paramType
              case a => filterType(a.tpt.toString)
            }.mkString(","),
            (if (params.flatten.isEmpty) "" else ",") + returnType)
          logger.debug("********* TEMPLATE TYPE OK!!!!!!!!!!!***********")
          Thread.sleep(5000)
          root
        }

        val f = {
          logger.debug("********* FUNCTION TYPE!!!!!!!!!!!***********")
          Thread.sleep(5000)
          val function = "def f:%s = %s => apply%s".format(
            functionType,
            params.map(group => "(" + group.map(_.name.toString).mkString(",") + ")").mkString(" => "),
            params.map(group => "(" + group.map { p =>
              p.name.toString + Option(p.tpt.toString).filter(_.startsWith("_root_.scala.<repeated>")).map(_ => ":_*").getOrElse("")
            }.mkString(",") + ")").mkString)
          logger.debug("********* FUNCTION TYPE OK!!!!!!!!!!!***********")
          Thread.sleep(5000)
          function
        }

        (renderCall, f, templateType)
      }.get(10000)

      logger.debug("********* RESP OK!!!!!!!!!!!***********")
      Thread.sleep(5000)

      resp match {
        case None => {
          logger.debug("********* RESP NONE!!!!!!!!!!!***********")
          Thread.sleep(5000)
          PresentationCompiler.global.reportThrowable(new Throwable("Timeout in getFunctionMapping"))
          ("", "", "")
        }
        case Some(res) =>
          res match {
            case Right(t) => {
              logger.debug("********* RIGHT!!!!!!!!!!!***********")
              Thread.sleep(5000)
              PresentationCompiler.global.reportThrowable(new Throwable("Throwable in getFunctionMapping", t))
              ("", "", "")
            }
            case Left(res) => {
              logger.debug("********* LEFT!!!!!!!!!!!***********")
              Thread.sleep(5000)
              res
            }
          }
      }
    }


    def myGenerateCode(packageName: String, name: String, root: Template, resultType: String, formatterType: String,
                     additionalImports: Seq[String], constructorAnnotations: Seq[String]): Seq[Any] = {
      val (renderCall, f, templateType) = mygetFunctionMapping(
        root.params.str,
        resultType)
      logger.debug("********* TemplateAsFunctionCompiler OK!!!!!!!!!!!!! ***********")
      Thread.sleep(5000)
      // Get the imports that we need to include, filtering out empty imports
      val imports: Seq[Any] = Seq(additionalImports.map(i => Seq("import ", i, "\n")),
        formatImports(root.topImports))
      logger.debug("********* IMPORTS OK ***********")
      Thread.sleep(5000)
      val classDeclaration = root.constructor.fold[Seq[Any]](
        Seq("object ", name)
      ) { constructor =>
        Vector.empty :+ "/*" :+ constructor.comment.fold("")(_.msg) :+ """*/
class """ :+ name :+ " " :+ constructorAnnotations :+ " " :+ Source(constructor.params.str, constructor.params.pos)
      }
      logger.debug("*********CLASS DECLARATIONS OK ***********")
      Thread.sleep(5000)
      val generated = {
        Vector.empty :+ """
package """ :+ packageName :+ """

""" :+ imports :+ """
                  """ :+ classDeclaration :+ """ extends _root_.play.twirl.api.BaseScalaTemplate[""" :+ resultType :+ """,_root_.play.twirl.api.Format[""" :+ resultType :+ """]](""" :+ formatterType :+ """) with """ :+ templateType :+ """ {

  /*""" :+ root.comment.map(_.msg).getOrElse("") :+ """*/
  def apply""" :+ Source(root.params.str, root.params.pos) :+ """:""" :+ resultType :+ """ = {
    _display_ {
      {
""" :+ mytemplateCode(root, resultType) :+ """
      }
    }
  }

  """ :+ renderCall :+ """

  """ :+ f :+ """

  def ref: this.type = this

}

"""
      }

      logger.debug("********* MY GENERATECODE OK!!!!!!!! ***********")
      Thread.sleep(5000)
      generated
    }

    def myGenerateFinalTemplate(absolutePath: String, contents: Array[Byte], packageName: String, name: String,
                              root: Template, resultType: String, formatterType: String, additionalImports: Seq[String],
                              constructorAnnotations: Seq[String]): String = {
      val generated = myGenerateCode(packageName, name, root, resultType, formatterType, additionalImports,
        constructorAnnotations)

      Source.finalSource(absolutePath, contents, generated, Hash(contents, additionalImports))
    }

    def myparseAndGenerateCode(templateName: Array[String], content: Array[Byte], codec: Codec, absolutePath: String,
                             resultType: String, formatterType: String, additionalImports: Seq[String], constructorAnnotations: Seq[String],
                             inclusiveDot: Boolean) = {
      val templateParser = new MyTwirlParser(inclusiveDot)
      templateParser.parse(new String(content, codec.charSet)) match {
        case templateParser.Success(parsed: Template, rest) if rest.atEnd => {
          logger.debug("********* TEMPLATE PARSER SUCCESS REST.ATEND ***********")
          Thread.sleep(5000)
          myGenerateFinalTemplate(absolutePath,
            content,
            templateName.dropRight(1).mkString("."),
            templateName.takeRight(1).mkString,
            parsed,
            resultType,
            formatterType,
            additionalImports,
            constructorAnnotations
          )
        }
        case templateParser.Success(_, rest) => {
          logger.debug("********* TEMPLATE PARSER SUCCESS ***********")
          Thread.sleep(5000)
          throw new TemplateCompilationError(new File(absolutePath), "Not parsed?", rest.pos.line, rest.pos.column)
        }
        case templateParser.Error(_, rest, errors) => {
          logger.debug("********* TEMPLATE PARSER ERRORS --> ")
          errors.foreach(e => logger.error(s"ERROR -> ${e.str}"))
          val firstError = errors.head
          throw new TemplateCompilationError(new File(absolutePath), firstError.str, firstError.pos.line, firstError.pos.column)
        }
      }
    }

    def generatedFile(template: File, codec: Codec, sourceDirectory: File, generatedDirectory: File, inclusiveDot: Boolean) = {
      val templateName = {
        logger.debug("SOURCE2 TEMPLATE NAME....")
        val name = source2TemplateName(template, sourceDirectory, template.getName.split('.').takeRight(1).head).split('.')
        logger.debug("NAME OK!!!!....")
        if (inclusiveDot) addInclusiveDotName(name) else name
      }
      templateName -> GeneratedSource(new File(generatedDirectory, templateName.mkString("/") + ".template.scala"), codec)
    }

    def compile[T](templateName: String, className: String, additionalImports: Seq[String] = Nil): Option[CompiledTemplate[T]] = {
      val templateFile = new File(sourceDir, templateName)
      if (templateFile.exists()) logger.debug("Templatefile created!!!!!") else logger.error("Unable to create the template file")
      val generatedTemplate = myCompilation(templateFile, sourceDir, generatedDir, "play.twirl.api.TxtFormat",
          additionalImports = TwirlCompiler.DefaultImports ++ additionalImports)
      if (generatedTemplate.isDefined) {
        logger.debug("Template generated fine!!!!!!")
        val template = generatedTemplate.get
        val mapper = GeneratedSource(template)
        logger.debug("About to compiler run.......")
        val run = new compiler.Run
        compileErrors.clear()
        run.compile(List(template.getAbsolutePath))

        compileErrors.headOption.foreach {
          case CompilationError(msg, line, column) =>
            logger.error(s"There's been a template compilation error [msg-line-column]: $msg-$line-$column")
            compileErrors.clear()
            throw CompilationError(msg, mapper.mapLine(line), mapper.mapPosition(column))
        }
        logger.debug("Compiling OK!!!!!!")
        Option(new CompiledTemplate[T](className, classloader))
      } else {
        logger.debug("There's been an error compiling the template")
        None
      }
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

private object TwirlIO2 {

  val defaultEncoding = scala.util.Properties.sourceEncoding

  val defaultCodec = Codec(defaultEncoding)

  /**
    * Read the given stream into a byte array.
    *
    * Does not close the stream.
    */
  def readStream(stream: InputStream): Array[Byte] = {
    val buffer = new Array[Byte](8192)
    var len = stream.read(buffer)
    val out = new ByteArrayOutputStream()
    while (len != -1) {
      out.write(buffer, 0, len)
      len = stream.read(buffer)
    }
    out.toByteArray
  }

  /**
    * Read the file as a String.
    */
  def readFile(file: File): Array[Byte] = {
    val is = new FileInputStream(file)
    try {
      readStream(is)
    } finally {
      closeQuietly(is)
    }
  }

  /**
    * Read the given stream into a String.
    *
    * Does not close the stream.
    */
  def readStreamAsString(stream: InputStream, codec: Codec = defaultCodec): String = {
    new String(readStream(stream), codec.name)
  }

  /**
    * Read the URL as a String.
    */
  def readUrlAsString(url: URL, codec: Codec = defaultCodec): String = {
    val is = url.openStream()
    try {
      readStreamAsString(is, codec)
    } finally {
      closeQuietly(is)
    }
  }

  /**
    * Read the file as a String.
    */
  def readFileAsString(file: File, codec: Codec = defaultCodec): String = {
    val is = new FileInputStream(file)
    try {
      readStreamAsString(is, codec)
    } finally {
      closeQuietly(is)
    }
  }

  /**
    * Write the given String to a file
    */
  def writeStringToFile(file: File, contents: String, codec: Codec = defaultCodec) = {
    if (!file.getParentFile.exists) {
      file.getParentFile.mkdirs()
    }
    val writer = new OutputStreamWriter(new FileOutputStream(file), codec.name)
    try {
      writer.write(contents)
    } finally {
      closeQuietly(writer)
    }
  }

  /**
    * Close the given closeable quietly.
    *
    * Ignores any IOExceptions encountered.
    */
  def closeQuietly(closeable: Closeable) = {
    try {
      if (closeable != null) {
        closeable.close()
      }
    } catch {
      case e: IOException => // Ignore
    }
  }

  /**
    * Delete the given directory recursively.
    */
  def deleteRecursively(dir: File) {
    if (dir.isDirectory) {
      dir.listFiles().foreach(deleteRecursively)
    }
    dir.delete()
  }
}
