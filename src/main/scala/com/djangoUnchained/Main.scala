package com.djangoUnchained

import cats.effect.{ExitCode, IO, IOApp}

/**
 * Created by Bhavya Jain.
 * 2023-01-14
 */
object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    CallbackServer.stream[IO].compile.drain.as(ExitCode.Success)
}
