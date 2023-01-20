package com.djangoUnchained.MockDatabase

import org.scalacheck.Gen

import scala.io.Source
import scala.util.matching.Regex
import scala.util.{Try, Using}

/**
 * Created by Bhavya Jain.
 * 2023-01-13
 */
trait MoviesRepo {
  def spawnMovieMessageWithGenre(genre: String): String
}

object MoviesRepo {
  private val emptyMovie = Movie(
    title = "",
    releaseYear = 0,
    genres = List.empty,
    duration = "",
    IMDBRating = 0.0
  )

  val Thriller: String = "Thriller"
  val Action: String = "Action"
  val Drama: String = "Drama"
  val Crime: String = "Crime"
  val Scifi: String = "Sci-Fi"
  val Comedy: String = "Comedy"

  def impl(fileName: String): MoviesRepo = new MoviesRepo {
    private val movieList: List[Movie] = Movie.readMovieCSV(fileName)

    override def spawnMovieMessageWithGenre(genre: String): String =
      genMovieWithGenre(movieList, genre).sample.getOrElse(emptyMovie).toString

    private def genMovieWithGenre(movieList: List[Movie], genre: String): Gen[Movie] =
      Gen.oneOf(movieList.filter(_.genres.contains(genre)))
  }

  case class Movie(
                    title: String,
                    releaseYear: Int,
                    genres: List[String],
                    duration: String,
                    IMDBRating: Double
                  ) {
    override def toString: String =
      s"Title: *${this.title}*\nRelease Year: *${this.releaseYear}*\nGenres: *${this.genres.mkString(", ")}*\n" +
        s"IMDB Rating: *${this.IMDBRating}*\nDuration: *${this.duration}*"
  }

  object Movie {
    // Mock Movie Database
    private def parsedMovie(line: String): Option[Movie] = {
      val movieLinePattern: Regex = """^(\d+),\"?(\d+). ([\x00-\x7F]+) \((\d+)\)\"?,([^,]+),([^,]+),\"?([^\"]+)\"?,(\d).(\d),([\x00-\x7F]+)$""".r
      Try {
        line match {
          case movieLinePattern(_, _, title, year, _, duration, genres, rating_1, rating_2, _) =>
            Movie(
              title = title,
              releaseYear = year.toInt,
              genres = genres.split(", ").toList,
              duration = duration,
              IMDBRating = (rating_1 + "." + rating_2).toDouble
            )
        }
      }.toOption
    }

    def readMovieCSV(filename: String): List[Movie] = {
      Using(Source.fromFile(filename)("UTF-8")) { source =>
        source
          .getLines()
          .flatMap(parsedMovie)
          .toList
      }.get
    }
  }
}