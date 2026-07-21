package sych.ad.scrapper.service.data.orm.tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class UsersTable(tag: Tag) extends Table[(Long, Long)](tag, "users") {
  def userId: Rep[Long]   = column[Long]("user_id")
  def tgChatId: Rep[Long] = column[Long]("tg_chat_id")

  def * : ProvenShape[(Long, Long)] = (userId, tgChatId)
}

object UsersTable {
  val query: TableQuery[UsersTable] = TableQuery[UsersTable]
}
