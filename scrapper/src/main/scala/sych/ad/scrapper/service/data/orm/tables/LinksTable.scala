package sych.ad.scrapper.service.data.orm.tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import sych.ad.scrapper.dto.LinkTableTypes

import java.sql.Timestamp

class LinksTable(tag: Tag)
  extends Table[LinkTableTypes.linkInfoType](
    tag,
    "links"
  ) {
  def id: Rep[Long]                            = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def url: Rep[String]                         = column[String]("url", O.Unique)
  def lastUpdatedAt: Rep[Option[Timestamp]]    = column[Option[Timestamp]]("last_updated_at")
  def lastUpdateTitle: Rep[Option[String]]     = column[Option[String]]("last_update_title")
  def lastUpdateUserName: Rep[Option[String]]  = column[Option[String]]("last_update_user_name")
  def lastUpdatePreview: Rep[Option[String]]   = column[Option[String]]("last_update_preview")
  def lastUpdateEventType: Rep[Option[String]] = column[Option[String]]("last_update_event_type")

  def *
      : ProvenShape[LinkTableTypes.linkInfoType] =
    (id, url, lastUpdatedAt, lastUpdateTitle, lastUpdateUserName, lastUpdatePreview, lastUpdateEventType)
}

object LinksTable {
  val query: TableQuery[LinksTable] = TableQuery[LinksTable]
}
