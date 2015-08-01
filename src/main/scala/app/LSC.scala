package app

import java.io._
import java.util.Date

import org.apache.commons.cli.{HelpFormatter, Options, DefaultParser, CommandLineParser}
import org.jsoup.Jsoup
import org.jsoup.examples.HtmlToPlainText
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._


/**
 * LSC Main Application
 */
object LSC extends App {


  val hpt = new HtmlToPlainText()

  private var outputDir: String = ""

  private def body(doc: Document, writer: BufferedWriter): Unit = {
    val cont = hpt.getPlainText(doc.select("div.b-story-body-x.x-r15").first())
    writer.write(cont)
    writer.write("\n--------------------\n")
    writer.flush()
    val next = doc.select("a.b-pager-next")
    if (!next.isEmpty) {
      body(Jsoup.connect(next.first().attr("href")).get(), writer)
    }
  }

  private def download(link: String): Unit = {
    try {
      val doc: Document = Jsoup.connect(link).get()
      val title = doc.select("div.b-story-header h1").text()
      val author = doc.select("span.b-story-user-y.x-r22 a").text()
      val name = s"$outputDir${if (!outputDir.endsWith("/")) "/"}" +
        (author + "_" + title).replaceAll("\\s", "_") + ".txt"

      val writer = new BufferedWriter(new FileWriter(new File(name)))

      writer.write(s"$title by $author\n ($link)\n\n")
      body(doc, writer)
      writer.write(s"\n\n############\nFetched on ${new Date().toString}")
      writer.flush()
      writer.close()
    } catch {
      case e: IOException =>
        System.exit(1)

    }
  }

  private def readAllAuthor(link: String): Unit = {
    try {
      val doc: Document = Jsoup.connect(link).get()
      val name: String = doc.select("a.contactheader").text()
      println(s"Grabbing stories for $name")
      val urls = doc.select("tr.sl td a.bb").iterator().toList
      println(s"${urls.length} stories found")
      urls.foreach{x=>download(x.attr("href"))}
    } catch {
      case e: IOException =>
        System.exit(1)
    }
  }

  val cli: CommandLineParser = new DefaultParser()
  val cliOptions: Options = new Options()
  val formatter = new HelpFormatter()
  cliOptions.addOption("a", "author", true, "grab author")
  cliOptions.addOption("s", "story", true, "grab story")
  cliOptions.addOption("p", "port", true, "port to connect to")
  cliOptions.addOption("o", "out", true, "output directory")

  //Main
  try {

    val parsed = cli.parse(cliOptions, args)

    System.setProperty("socksProxyHost", "127.0.0.1")
    System.setProperty("socksProxyPort",
      Option(parsed.getOptionValue("p")).getOrElse("8081")
    )

    if (parsed.hasOption("o")) {
      outputDir = parsed.getOptionValue("o")
    }

    if (parsed.hasOption("a")) {
      readAllAuthor(parsed.getOptionValue("a"))
      println("Downloaded author")
    } else if (parsed.hasOption("s")){
      download(parsed.getOptionValue("s"))
      println("Downloaded story")
    } else {
      formatter.printHelp( "LSC", cliOptions );
    }
  } catch {
    case e: IndexOutOfBoundsException =>
      formatter.printHelp( "LSC", cliOptions );
  }
}
