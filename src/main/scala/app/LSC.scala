package app

import java.io._
import java.util.Date

import org.jsoup.Jsoup
import org.jsoup.examples.HtmlToPlainText
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._


/**
 * LSC Main Application
 */
object LSC extends App {
  System.setProperty("socksProxyHost", "127.0.0.1")
  System.setProperty("socksProxyPort", "8081")


  val hpt = new HtmlToPlainText()

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
      val name = (author + "_" + title).replaceAll("\\s", "_") + ".txt"

      val writer = new BufferedWriter(new FileWriter(new File(name)))

      writer.write(s"$title by $author\n\n")
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
      val urls = doc.select("tr.root-story.r-ott td a.t-t84.bb.nobck").iterator().toList
      urls.foreach{x=>download(x.attr("href"))}
    } catch {
      case e: IOException =>
        System.exit(1)
    }
  }

  private def printUsage(): Unit = {
    println(
    """
       Usage:
          java -jar LSC-assembly-1.0.jar [--author] URL
    """.stripMargin
    )
  }

  // Main
  try {
    val arg0 = args(0)
    if (arg0 == "--author") {
      readAllAuthor(args(1))
      println("Downloaded author")
    } else {
      download(arg0)
      println("Downloaded story")
    }
  } catch {
    case e: IndexOutOfBoundsException =>
      printUsage()
  }
}
