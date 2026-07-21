package sych.ad.scrapper.server

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sttp.tapir.server.ServerEndpoint
import sych.ad.common.controller.Controller
import sych.ad.common.endpoints.ScrapperEndpoints
import sych.ad.common.util.LinkParser
import sych.ad.scrapper.valkey.ValkeyCache
import sych.ad.common.dto.ApiErrorResponse._
import sych.ad.common.dto.{AddLinkRequest, ApiErrorResponse, LinkResponse, ListLinksResponse, RemoveLinkRequest}
import sych.ad.scrapper.service.data.DataService

class ScrapperController(
    dataService: DataService,
    getLinksCache: ValkeyCache[Long, ListLinksResponse]
)(implicit L: LoggerFactory[IO]) extends Controller {

  private val logger = L.getLogger

  private val registerChat: ServerEndpoint.Full[Unit, Unit, (Long, Long), ApiErrorResponse, Unit, Any, IO] =
    ScrapperEndpoints.registerChat
      .serverLogic { case (tgChatId, userId) => dataService.registerChat(userId, tgChatId).toApiError }

  private val deleteChat: ServerEndpoint.Full[Unit, Unit, (Long, Long), ApiErrorResponse, Unit, Any, IO] =
    ScrapperEndpoints.deleteChat
      .serverLogic { case (tgChatId, userId) => dataService.deleteChat(userId, tgChatId).toApiError }

  private val getLinks: ServerEndpoint.Full[Unit, Unit, (Long, Long), ApiErrorResponse, ListLinksResponse, Any, IO] =
    ScrapperEndpoints.getLinks
      .serverLogic { case (tgChatId, userId) =>
        (for {
          cachedResult <- getLinksCache.get(tgChatId)
          result       <- cachedResult.fold(dataService.getLinks(userId, tgChatId))(IO.pure)
          _            <- IO.whenA(cachedResult.isEmpty)(getLinksCache.set(tgChatId, result))
        } yield result).toApiError
      }

  private val addLink
      : ServerEndpoint.Full[Unit, Unit, (Long, Long, AddLinkRequest), ApiErrorResponse, LinkResponse, Any, IO] =
    ScrapperEndpoints.addLink
      .serverLogic { case (tgChatId, userId, addLinkRequest) =>
        for {
          _      <- logger.info(s"Processing link: chatId=$tgChatId, userId=$userId, link=${addLinkRequest.link}")
          result <- if (LinkParser.isValidLink(addLinkRequest.link)) {
            logger.info("Link is valid").flatMap { _ =>
              (for {
                result <- dataService.addLink(userId, tgChatId, addLinkRequest)
                _      <- getLinksCache.del(tgChatId) // invalidate cache
              } yield result).toApiError
            }
          } else {
            logger.info(s"Invalid link format: ${addLinkRequest.link}").flatMap(_ =>
              IO.pure(Left(ApiErrorResponse(
                description = "Invalid link",
                code = "400",
                exceptionName = "IllegalArgumentException",
                exceptionMessage = "Invalid link format",
                stacktrace = List.empty
              )))
            )
          }
          _ <- logger.debug(s"Result: $result")
        } yield result
      }

  private val deleteLink
      : ServerEndpoint.Full[Unit, Unit, (Long, Long, RemoveLinkRequest), ApiErrorResponse, LinkResponse, Any, IO] =
    ScrapperEndpoints.deleteLink
      .serverLogic { case (tgChatId, userId, removeLinkRequest) =>
        (for {
          result <- dataService.deleteLink(userId, tgChatId, removeLinkRequest)
          _      <- getLinksCache.del(tgChatId) // invalidate cache
        } yield result).toApiError
      }

  override val endpoints: List[ServerEndpoint[Any, IO]] = List(
    registerChat,
    deleteChat,
    getLinks,
    addLink,
    deleteLink
  )
}
