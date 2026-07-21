package sych.ad.bot.handler

import telegramium.bots.ChatIntId

sealed trait Reaction

case class SendMessage(
    chatId: ChatIntId,
    text: String
) extends Reaction
