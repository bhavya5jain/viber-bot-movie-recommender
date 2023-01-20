package com.djangoUnchained

import cats.effect.Sync
import cats.implicits._
import com.djangoUnchained.ViberAPIOps.ViberCallback.Callback
import com.djangoUnchained.ViberAPIOps.ViberSendMessage
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

/**
 * Created by Bhavya Jain.
 * 2023-01-15
 */
object CallbackRoutes {

  def callbackRoutes[F[_]: Sync](viberSendMessage: ViberSendMessage[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "healthcheck" => Ok("Movie Recommender service healthy!")

      case request @ POST -> Root =>
        for {
          callback <- request.as[Callback]
          _ <- viberSendMessage.sendMessage(callback)
          resp <- Ok(callback)
        } yield resp
      case request @ POST -> Root / "return" =>
        Ok(request.body)
    }
  }
}
