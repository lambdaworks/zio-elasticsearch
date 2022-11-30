package zio

import zio.prelude.Assertion._
import zio.prelude.{Newtype, Subtype}

package object elasticsearch {
  object Routing extends Subtype[String] {
    override def assertion = assert {
      !isEmptyString
    }
  }
  type Routing = Routing.Type

  object DocumentId extends Newtype[String]
  type DocumentId = DocumentId.Type

  object IndexName extends Newtype[String]
  type IndexName = IndexName.Type

}
