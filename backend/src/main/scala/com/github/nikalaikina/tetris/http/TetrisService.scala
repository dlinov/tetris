package com.github.nikalaikina.tetris.http

import java.net.URL

import cats.implicits._
import cats.data._
import cats.effect.Effect
import org.http4s._
import org.http4s.CacheDirective._
import org.http4s.MediaType._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.server.middleware.GZip
import scalatags.Text.TypedTag
import scalatags.Text.all.Modifier

object TetrisService {

  // TODO: find a way not to hardcode script names
  val jsScript = "frontend-fastopt.js"
  val jsDeps = "frontend-jsdeps.js"
  val jsScripts: Seq[Modifier] = {
    import scalatags.Text.all._
    List(
      script(src := jsScript),
      script(src := jsDeps),
      script("GameApp.main()")
    )
  }

  val index: Seq[Modifier] = {
    import scalatags.Text.all._
    Seq(
      p(
        style := "font-size: 20px;",
        span("Score: "),
        span(id := "score", "")),
      canvas(id := "tetris", widthA := 200, heightA := 380)
    )
  }

  def template(
                headContent: Seq[Modifier],
                bodyContent: Seq[Modifier],
                scripts: Seq[Modifier],
                cssComps: Seq[Modifier]): TypedTag[String] = {
    import scalatags.Text.all._

    html(
      head(
        headContent,
        cssComps
      ),
      body(
        bodyContent,
        scripts
      )
    )

  }

  val supportedStaticExtensions =
    List(".html", ".js", ".map", ".css", ".png", ".ico")

  def service[F[_]](implicit F: Effect[F]): HttpService[F] = GZip {
    def getResource(pathInfo: String): F[URL] = F.delay(getClass.getResource(pathInfo))

    object dsl extends Http4sDsl[F]
    import dsl._

    HttpService[F] {

      case GET -> Root =>
        Ok(template(Seq(), index, jsScripts, Seq()).render)
          .map(
            _.withContentType(`Content-Type`(`text/html`, Charset.`UTF-8`))
              .putHeaders(`Cache-Control`(NonEmptyList.of(`no-cache`())))
          )

      case req if supportedStaticExtensions.exists(req.pathInfo.endsWith) =>
        StaticFile.fromResource[F](req.pathInfo, req.some)
          .orElse(OptionT.liftF(getResource(req.pathInfo)).flatMap(StaticFile.fromURL[F](_, req.some)))
          .map(_.putHeaders(`Cache-Control`(NonEmptyList.of(`no-cache`()))))
          .fold(NotFound())(_.pure[F])
          .flatten

    }
  }

}
