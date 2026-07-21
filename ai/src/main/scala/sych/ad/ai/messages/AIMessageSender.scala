package sych.ad.ai.messages

import sych.ad.ai.config.AIProcessingConfig
import sych.ad.ai.processing.AIProcessor
import sych.ad.common.dto.LinkUpdate

trait AIMessageSender {
  def process(update: LinkUpdate, config: AIProcessingConfig): Option[(String, List[Long])]
}

object AIMessageSender extends AIMessageSender {

  override def process(update: LinkUpdate, config: AIProcessingConfig): Option[(String, List[Long])] = {
    val message = update.description

    val isFiltered =
      AIProcessor.filterAuthor(message, config.blockedAuthors) ||
        AIProcessor.filterStopWords(message, config.stopWords)

    if (isFiltered) {
      None
    } else {
      val truncated = AIProcessor.truncateText(message, config.maxLength)
      Some((truncated, update.tgChatIds))
    }
  }
}
