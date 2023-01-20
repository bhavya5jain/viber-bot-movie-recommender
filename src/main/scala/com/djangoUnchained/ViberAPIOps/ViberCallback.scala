package com.djangoUnchained.ViberAPIOps

import cats.effect.Sync
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

/**
 * Created by Bhavya Jain.
 * 2023-01-15
 */

object ViberCallback {
  val EmptyUser = User(
    id = "",
    name = None,
    avatar = None,
    country = None,
    language = "EN",
    api_version = 1
  )
  val EmptyMessage = Message(
    `type` = "",
    text = None,
    media = None,
    tracking_data = None,
    file_name = None,
    file_size = None,
    duration = None,
    sticker_id = None
  )

  case class Callback(
                       event: String,
                       timestamp: Long,
                       message_token: Long,
                       sender: Option[User],
                       message: Option[Message],
                       user_id: Option[String],
                       user: Option[User],
                       desc: Option[String]
                     )

  case class User(id: String, name: Option[String], avatar: Option[String], country: Option[String], language: String, api_version: Int)

  case class Message(
                      `type`: String,
                      text: Option[String],
                      media: Option[String],
                      tracking_data: Option[String],
                      file_name: Option[String],
                      file_size: Option[Int],
                      duration: Option[Int],
                      sticker_id: Option[String]
                    )

  object Callback {
    implicit val callbackDecoder: Decoder[Callback] = deriveDecoder[Callback]
    implicit def callbackEntityDecoder[F[_] : Sync]: EntityDecoder[F, Callback] = jsonOf
    implicit val callbackEncoder: Encoder[Callback] = deriveEncoder[Callback]
    implicit def callbackEntityEncoder[F[_] : Sync]: EntityEncoder[F, Callback] = jsonEncoderOf
  }

  object User {
    implicit val userDecoder: Decoder[User] = deriveDecoder[User]
    implicit def userEntityDecoder[F[_] : Sync]: EntityDecoder[F, User] = jsonOf
    implicit val userEncoder: Encoder[User] = deriveEncoder[User]
    implicit def userEntityEncoder[F[_] : Sync]: EntityEncoder[F, User] = jsonEncoderOf
  }

  object Message {
    implicit val messageDecoder: Decoder[Message] = deriveDecoder[Message]
    implicit def messageEntityDecoder[F[_] : Sync]: EntityDecoder[F, Message] = jsonOf
    implicit val messageEncoder: Encoder[Message] = deriveEncoder[Message]
    implicit def messageEntityEncoder[F[_] : Sync]: EntityEncoder[F, Message] = jsonEncoderOf
  }
}
