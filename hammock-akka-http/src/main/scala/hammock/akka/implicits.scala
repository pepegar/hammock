package hammock
package akka

import _root_.akka.http.scaladsl.marshalling._
import _root_.akka.http.scaladsl.model.RequestEntity
import _root_.akka.http.scaladsl.model.HttpEntity

trait Implicits {

  implicit def codecToEntityMarshaller[A : Codec]: ToEntityMarshaller[A] = Marshaller.strict { a =>
    Marshalling.Opaque(() => HttpEntity(Codec[A].encode(a)))
  }

}

object implicits extends Implicits
