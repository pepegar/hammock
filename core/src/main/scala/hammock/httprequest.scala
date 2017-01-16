package hammock

import cats._
import cats.data.Kleisli
import cats.free.Free
import org.apache.http.client.HttpClient


object httprequest {

  trait HttpResponse

  sealed abstract class HttpRequestF[A](url: String, headers: Map[String, String], body: Option[String]) extends Product with Serializable
  case class Options(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Get(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Head(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Post(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Put(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Delete(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Trace(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)
  case class Connect(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse](url, headers, body)

  type HttpRequestIO[A] = Free[HttpRequestF, A]

  object Ops {
    def options(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Options(url, headers, body))
    def get(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Get(url, headers, body))
    def head(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Head(url, headers, body))
    def post(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Delete(url, headers, body))
    def trace(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Trace(url, headers, body))
    def connect(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Connect(url, headers, body))
  }

  object Interp {
    def trans[F[_]](implicit httpClient: HttpClient, ME: MonadError[F, Throwable]) = transK andThen λ[Kleisli[F, HttpClient, ?] ~> F](_.run(httpClient))

    def transK[F[_]](implicit ME: MonadError[F, Throwable]): HttpRequestF ~> Kleisli[F, HttpClient, ?] = λ[HttpRequestF ~> Kleisli[F, HttpClient, ?]](_ match {
      case Options(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Get(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Head(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Post(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Put(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Delete(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Trace(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
      case Connect(url, headers, body) => Kleisli { client =>
        ME.catchNonFatal {
          ???
        }
      }
    })

  }
}
