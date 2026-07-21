package sych.ad.scrapper.service.data.orm.tables

import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import slick.lifted.ProvenShape

class UserLinksTable(tag: Tag) extends Table[(Long, Long, Long, Timestamp)](tag, "user_links") {
  def userId: Rep[Long]         = column[Long]("user_id")
  def tgChatId: Rep[Long]       = column[Long]("tg_chat_id")
  def linkId: Rep[Long]         = column[Long]("link_id")
  def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

  def * : ProvenShape[(Long, Long, Long, Timestamp)] = (userId, tgChatId, linkId, createdAt)
}

object UserLinksTable {
  val query: TableQuery[UserLinksTable] = TableQuery[UserLinksTable]
}
