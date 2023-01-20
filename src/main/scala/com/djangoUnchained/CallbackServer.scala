package com.djangoUnchained

import cats.effect.{ConcurrentEffect, ExitCode, Sync, Timer}
import com.djangoUnchained.MockDatabase.MoviesRepo
import com.djangoUnchained.ViberAPIOps.ViberSendMessage
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

/**
 * Created by Bhavya Jain.
 * 2023-01-13
 */
object CallbackServer {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F]): Stream[F, ExitCode] = {

    val movieCSVPath = "src/main/resources/csv/IMDB-top-1000.csv"

    for {
      _ <- Stream.eval(Sync[F].delay(println("Starting viber movie recommender server!")))
      client <- Stream.resource(BlazeClientBuilder[F](global).resource)
      movieRepo = MoviesRepo.impl(fileName = movieCSVPath)
      sendMessage = ViberSendMessage.impl(client, movieRepo)
      httpApp = CallbackRoutes.callbackRoutes[F](sendMessage).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "localhost")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }
}