package hammock
package akka

import _root_.akka.http.scaladsl.marshalling._
import _root_.akka.http.scaladsl.unmarshalling._
import _root_.akka.http.scaladsl.model.HttpEntity

trait Implicits {
  implicit def encoderToEntityMarshaller[A: Encoder]: ToEntityMarshaller[A] = Marshaller.strict { a =>
    Encoder[A].encode(a) match {
      case Entity.StringEntity(body, _) => Marshalling.Opaque(() => HttpEntity(body))
    }
  }

  implicit def decoderFromEntityUnmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    PredefinedFromEntityUnmarshallers.stringUnmarshaller
      .map(str => Decoder[A].decode(Entity.StringEntity(str)))
      .map(_.fold(throw _, identity))
}

object implicits extends Implicits
