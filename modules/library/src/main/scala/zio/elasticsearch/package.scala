package zio

import org.apache.commons.lang3.StringUtils
import zio.prelude.Assertion._
import zio.prelude.{AssertionError, Newtype}

package object elasticsearch {
  object Routing extends Newtype[String] {
    override def assertion = assert { // scalafix:ok
      !isEmptyString
    }
  }
  type Routing = Routing.Type

  object DocumentId extends Newtype[String]
  type DocumentId = DocumentId.Type

  object IndexName extends Newtype[String] {
    override def assertion = assertCustom { (x: String) => // scalafix:ok
      if (x.toLowerCase != x)
        Left(AssertionError.Failure("IndexName must be lower case only."))
      else if (StringUtils.startsWithAny(x, "+", "-", "_"))
        Left(AssertionError.Failure("IndexName cannot start with -, _, +."))
      else if (StringUtils.containsAny(x, '\\', '/', '*', '?', '"', '/', '<', '>', '|', ' ', ',', '#'))
        Left(
          AssertionError.Failure(
            "IndexName cannot include \\, /, *, ?, \", <, >, |, ` ` (space character), ,(comma), #"
          )
        )
      else if (StringUtils.contains(x, ":"))
        Left(AssertionError.Failure("""IndexName cannot include ":"(since 7.0)."""))
      else if (StringUtils.equalsAny(x, ".", ".."))
        Left(AssertionError.Failure("""IndexName cannot be . or .."""))
      else if (x.getBytes().length > 255)
        Left(
          AssertionError.Failure(
            "IndexName cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster)"
          )
        )
      else if (StringUtils.startsWith(x, ".")) {
        // todo: Warning should be that IndexNames starting with . are deprecated?
        Right(())
      } else
        Right(())
    }
  }
  type IndexName = IndexName.Type

}
