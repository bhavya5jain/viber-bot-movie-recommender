package com.djangoUnchained.ViberAPIOps

import cats.effect.Sync
import com.djangoUnchained.MockDatabase.MoviesRepo._
import com.djangoUnchained.MockDatabase.MoviesRepo
import com.djangoUnchained.ViberAPIOps.ViberCallback._
import com.djangoUnchained.ViberAPIOps.ViberSendMessage.Response
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, EntityEncoder, Header, Request, Uri}

/**
 * Created by Bhavya Jain.
 * 2023-01-16
 */
trait ViberSendMessage[F[_]] {
  def sendMessage(callback: Callback): F[Response]
}

object ViberSendMessage {
  private val viberSendMessageURI: Uri = uri"https://chatapi.viber.com/pa/send_message"
//  private val viberSendMessageURI: Uri = uri"http://localhost:8081/return"
  private val viberAuthToken = ??? // Put Viber Authorization Token here
  private val EmptyResponse = Response(
    status = None,
    status_message = None,
    message_token = None,
    chat_hostname = None,
    billing_status = None
  )
  private val defaultMessage = "I am not yet smart enough to understand this\nType '*Movie please*' to get a movie recommendation"
  private val defaultTextMessage = "I don't understand what this means\nType '*Movie please*' to get a movie recommendation"
  private val defaultWelcomeMessage = "Welcome to my world!\nI would help you with a movie recommendation\n\nJust type '*Movie please*' & choose a genre"

  def impl[F[_]: Sync](client: Client[F], moviesRepo: MoviesRepo): ViberSendMessage[F] = new ViberSendMessage[F] {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._
    import Syntax._

    def sendMessage(callback: Callback): F[Response] =
      callback.event match {
        case "message" => client.expect[Response](ToPostRequest(createSMMEventMessage(callback)))
        case "subscribed" => client.expect[Response](ToPostRequest(createSMMEventSubscribed(callback)))
        case _ => Sync[F].delay(EmptyResponse)
      }

    private def ToPostRequest(sendMessage: SendMessage): F[Request[F]] = POST(
      sendMessage.asJson,
      viberSendMessageURI,
      Header.Raw(CaseInsensitiveString("X-Viber-Auth-Token"), viberAuthToken)
    )

    private def ToSendMessage(receiver: String, text: String): SendMessage = SendMessage(receiver = receiver, text = Some(text))

    // SMM: Send Message Model
    private def createSMMEventMessage(callback: Callback): SendMessage = {
      val receiver = callback.sender.getOrElse(EmptyUser).id

      callback.message.getOrElse(EmptyMessage).`type` match {
        case "text" =>
          callback.message.getOrElse(EmptyMessage).text match {
            case Some("Movie please") => SendMessage(
              receiver = callback.sender.getOrElse(EmptyUser).id,
              min_api_version = 7,
              `type` = "rich_media",
              rich_media = Some(RichMedia(
                ButtonsGroupColumns = 6,
                ButtonsGroupRows = 6,
                Buttons = List(
                  Button(Columns = 6, Rows = 1, ActionBody = Thriller.recommend, Text = Thriller.buttonHTMLTag),
                  Button(Columns = 6, Rows = 1, ActionBody = Action.recommend, Text = Action.buttonHTMLTag),
                  Button(Columns = 6, Rows = 1, ActionBody = Drama.recommend, Text = Drama.buttonHTMLTag),
                  Button(Columns = 6, Rows = 1, ActionBody = Crime.recommend, Text = Crime.buttonHTMLTag),
                  Button(Columns = 6, Rows = 1, ActionBody = Scifi.recommend, Text = Scifi.buttonHTMLTag),
                  Button(Columns = 6, Rows = 1, ActionBody = Comedy.recommend, Text = Comedy.buttonHTMLTag),
                )
              ))
            )
            case Some("Recommend a Thriller") => ToSendMessage(receiver, moviesRepo.spawnMovieMessageWithGenre(Thriller))
            case Some("Recommend a Action") => ToSendMessage(receiver, moviesRepo.spawnMovieMessageWithGenre(Action))
            case Some("Recommend a Drama") => ToSendMessage(receiver, moviesRepo.spawnMovieMessageWithGenre(Drama))
            case Some("Recommend a Crime") => ToSendMessage(receiver, moviesRepo.spawnMovieMessageWithGenre(Crime))
            case Some("Recommend a Sci-Fi") => ToSendMessage(receiver, moviesRepo.spawnMovieMessageWithGenre(Scifi))
            case Some("Recommend a Comedy") => ToSendMessage(receiver, moviesRepo.spawnMovieMessageWithGenre(Comedy))
            case _ => ToSendMessage(receiver, defaultTextMessage)
          }
        case _ => ToSendMessage(receiver, defaultMessage)
      }
    }

    private def createSMMEventSubscribed(callback: Callback): SendMessage = SendMessage(
      receiver = callback.user.getOrElse(EmptyUser).id,
      text = Some(defaultWelcomeMessage)
    )
  }

  case class SendMessage(
                          receiver: String,
                          min_api_version: Int = 1,
                          tracking_data: Option[String] = None,
                          `type`: String = "text",
                          text: Option[String] = None,
                          rich_media: Option[RichMedia] = None,
                          keyboard: Option[String] = None
                        )

  case class Response(
                       status: Option[Int],
                       status_message: Option[String],
                       message_token: Option[Long],
                       chat_hostname: Option[String],
                       billing_status: Option[Int]
                     )

  case class RichMedia(
                        Type: String = "rich_media",
                        ButtonsGroupColumns: Int,
                        ButtonsGroupRows: Int,
                        BgColor: String = "#E4EAEF",
                        Buttons: List[Button]
                      )

  case class Button(
                     Columns: Int,
                     Rows: Int,
                     BgColor: String = "#2D2E2F",
                     ActionType: String = "reply",
                     ActionBody: String,
                     Text: String,
                     TextSize: String = "large",
                     TextVAlign: String = "middle",
                     TextHAlign: String = "middle"
                   )

  object SendMessage {
    implicit val decoder: Decoder[SendMessage] = deriveDecoder[SendMessage]
    implicit def EntityDecoder[F[_] : Sync]: EntityDecoder[F, SendMessage] = jsonOf
    implicit val Encoder: Encoder[SendMessage] = deriveEncoder[SendMessage]
    implicit def EntityEncoder[F[_] : Sync]: EntityEncoder[F, SendMessage] = jsonEncoderOf
  }

  object Response {
    implicit val Decoder: Decoder[Response] = deriveDecoder[Response]
    implicit def EntityDecoder[F[_] : Sync]: EntityDecoder[F, Response] = jsonOf
    implicit val Encoder: Encoder[Response] = deriveEncoder[Response]
    implicit def EntityEncoder[F[_] : Sync]: EntityEncoder[F, Response] = jsonEncoderOf
  }

  object RichMedia {
    implicit val Decoder: Decoder[RichMedia] = deriveDecoder[RichMedia]
    implicit def EntityDecoder[F[_] : Sync]: EntityDecoder[F, RichMedia] = jsonOf
    implicit val Encoder: Encoder[RichMedia] = deriveEncoder[RichMedia]
    implicit def EntityEncoder[F[_] : Sync]: EntityEncoder[F, RichMedia] = jsonEncoderOf
  }

  object Button {
    implicit val Decoder: Decoder[Button] = deriveDecoder[Button]
    implicit def EntityDecoder[F[_] : Sync]: EntityDecoder[F, Button] = jsonOf
    implicit val Encoder: Encoder[Button] = deriveEncoder[Button]
    implicit def EntityEncoder[F[_] : Sync]: EntityEncoder[F, Button] = jsonEncoderOf
  }
}
