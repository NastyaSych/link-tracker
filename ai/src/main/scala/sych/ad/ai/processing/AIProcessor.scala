package sych.ad.ai.processing

object AIProcessor {

  def truncateText(text: String, maxLength: Int): String = {
    if (text.length <= maxLength) text
    else text.take(maxLength) + "..."
  }

  def filterAuthor(text: String, blockedAuthors: List[String]): Boolean = {
    blockedAuthors.exists(text.contains)
  }

  def filterStopWords(text: String, stopWords: List[String]): Boolean = {
    stopWords.exists(text.contains)
  }
}
