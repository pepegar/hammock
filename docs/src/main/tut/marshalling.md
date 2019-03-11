---
layout: docs
title: Marshalling
position: 4
---

# Marshalling

Hammock has the notion of marshalling at several levels.  First, it
provides the [`Codec` typeclass](#codecs), for low level operations,
and the [`MarshallF` algebra](#marshalling-algebra) for unmarshalling
operations suspended in `Free`.

## Codecs

For marshalling operations, Hammock provides `Encoder` and `Decoder`
typeclasses:

```
@typeclass trait Encoder[A] {
  def encode(a: A): Entity
}

@typeclass trait Decoder[A] {
  def decode(a: Entity): Either[CodecException, A]
}
```

These typeclasses convert values to and from Hammock `Entity`.

### Circe integration

Hammock is integrated with [circe](http://circe.io), you can use this
integration with the `hammock-circe` package.  This integration is
designed to be non-intrusive, since a simple import of `import
hammock.circe.implicits._` will give you all the funcionality you
need.  For example:

```scala mdoc
import hammock.{Encoder => HammockEncoder, Decoder => HammockDecoder, _}
import hammock.circe.implicits._
import io.circe.{Encoder => CirceEncoder, Decoder => CirceDecoder}

case class Car(brand: String, model: String)

implicit val carEncoder: CirceEncoder[Car] = CirceEncoder.forProduct2("brand", "model") { car =>
  (car.brand, car.model)
}

implicit val carDecoder: CirceDecoder[Car] = CirceDecoder.forProduct2("brand", "model")(Car)

val ent: Entity = HammockEncoder[Car].encode(Car("ford", "fiesta"))

val Right(fordFiesta) = HammockDecoder[Car].decode(ent)
```

## Marshalling algebra

Hammock also provides a marshalling algebra.  This algebra only
provides an operation, `unmarshall` that transforms `Entity`es into
`A`s for which we have a `Decoder`.
