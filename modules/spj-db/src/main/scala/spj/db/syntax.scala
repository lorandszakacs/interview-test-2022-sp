package spj.db

import spj.*

extension [S](codec: Codec[S]) {
  def newtype[T](using
      builder: monix.newtypes.HasBuilder.Aux[T, S],
      extractor: monix.newtypes.HasExtractor.Aux[T, S]
  ): Codec[T] = {
    Codec.from[T](
      enc = codec.contramap(extractor.extract),
      dec = codec.emap(v => builder.build(v).leftMap(_.toReadableString))
    )
  }
}

extension (c: Codec.type) {
  def from[A](enc: Encoder[A], dec: Decoder[A]): Codec[A] = {
    def initException = new java.lang.ExceptionInInitializerError(
      s"""
         |You tried creating a skunk.Codec from a
         |skunk.Encoder and skunk.Decoder that have different
         |type lists. These two lists have to match.
         |
         |This exception was thrown at initialization time.
         |
         |Check the definition of you Codec. If you still
         |think this is correct, then roll your own Codec.
         |
         |encoder.types.length = ${enc.types}
         |decoder.types.length = ${enc.types}
         |
         |encoder.types = ${enc.types.mkString(",")}
         |decoder.types = ${dec.types.mkString(",")}
         |"""
    )

    // N.B. this is literally the only place we use 'throw' in our entire app
    if (enc.types.length != dec.types.length) throw initException
    else {
      val encSet = enc.types.toSet
      val deCSet = dec.types.toSet
      if (encSet != deCSet)
        throw initException
    }
    new Codec[A] {
      override def types: List[skunk.data.Type] = enc.types

      override def decode(offset: Int, ss: List[Option[String]]): Either[Decoder.Error, A] =
        dec.decode(offset, ss)

      override def sql: cats.data.State[Int, String] = enc.sql

      override def encode(a: A): List[Option[String]] = enc.encode(a)
    }
  }
}
