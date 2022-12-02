package zio

import org.apache.commons.lang3.StringUtils
import zio.prelude.Assertion._
import zio.prelude.AssertionError.failure
import zio.prelude.Newtype

package object elasticsearch {
  object Routing extends Newtype[String] {
    override def assertion = assert(!isEmptyString) // scalafix:ok
  }
  type Routing = Routing.Type

  object DocumentId extends Newtype[String]
  type DocumentId = DocumentId.Type

  object IndexName extends Newtype[String] {
    override def assertion = assertCustom { (name: String) => // scalafix:ok
      if (name.toLowerCase != name)
        Left(failure("IndexName must be lower case only."))
      else if (StringUtils.startsWithAny(name, "+", "-", "_"))
        Left(failure("IndexName cannot start with -, _, +."))
      else if (StringUtils.containsAny(name, '\\', '/', '*', '?', '"', '/', '<', '>', '|', ' ', ',', '#'))
        Left(failure("IndexName cannot include \\, /, *, ?, \", <, >, |, ` ` (space character), ,(comma), #"))
      else if (StringUtils.contains(name, ":"))
        Left(failure("""IndexName cannot include ":"(since 7.0)."""))
      else if (StringUtils.equalsAny(name, ".", ".."))
        Left(failure("""IndexName cannot be . or .."""))
      else if (name.getBytes().length > 255)
        Left(
          failure(
            "IndexName cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster)"
          )
        )
      else if (StringUtils.startsWith(name, ".")) {
        // todo: Warning should be added that IndexNames starting with . are deprecated?
        Right(())
      } else
        Right(())
    }
  }
  type IndexName = IndexName.Type

}
