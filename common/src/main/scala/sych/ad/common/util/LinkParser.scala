package sych.ad.common.util

object LinkParser {

  def isValidLink(link: String): Boolean = {
    parseGithubLink(link).isDefined || parseStackOverflowLink(link).isDefined
  }

  def parseGithubLink(link: String): Option[(String, String)] = link match {
    case s"https://github.com/$user/$repo" =>
      Some((user, repo))
    case _ =>
      None
  }

  def parseStackOverflowLink(link: String): Option[Long] = link match {
    case s"https://stackoverflow.com/questions/$id" if id.toLongOption.isDefined =>
      Some(id.toLong)
    case s"https://stackoverflow.com/questions/$id/$_" if id.toLongOption.isDefined =>
      Some(id.toLong)
    case _ =>
      None
  }
}
