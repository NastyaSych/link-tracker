package sych.ad.scrapper.dto

import sych.ad.common.dto.LinkResponse

case class Link(
    id: Long,
    url: String,
    updateInfo: Option[LinkUpdatedInfo]
) {
  def toLinkResponse(wasBefore: Boolean = false): LinkResponse = LinkResponse(
    id,
    url,
    wasBefore
  )
}
