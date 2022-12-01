package zio

import zio.prelude.Assertion._
import zio.prelude.{AssertionError, Newtype}

package object elasticsearch {
  object Routing extends Newtype[String] {
    override def assertion = assert { // scalafix:ok
      !isEmptyString
    }
  }
  type Routing = Routing.Type

  object DocumentId extends Newtype[String] {}
  type DocumentId = DocumentId.Type

  object IndexName extends Newtype[String] {
    override def assertion = assertCustom { (x: String) => // scalafix:ok
      if (x.toLowerCase != x) Left(AssertionError.Failure("IndexName must be lower case only."))
      else if (x.startsWith("-") || x.startsWith("+") || x.startsWith("_"))
        Left(AssertionError.Failure("IndexName cannot start with -, _, +."))
      /*
       * Todo: What is the best way to check these chars?
       * */
      else if (x.exists(char => raw"""\/*?"<>|,#""".contains(char)) || x.contains(' '))
        Left(
          AssertionError.Failure(
            "IndexName cannot include \\, /, *, ?, \", <, >, |, ` ` (space character), ,(comma), #"
          )
        )
      else if (x.contains(':'))
        Left(AssertionError.Failure("""IndexName cannot include ":"(since 7.0)."""))
      else if (x == "." || x == "..")
        Left(AssertionError.Failure("""IndexName cannot be . or .."""))
      else if (x.getBytes().length > 255)
        Left(
          AssertionError.Failure(
            """IndexName cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster)"""
          )
        )
      else if (x.startsWith(".")) {
        // todo: Warning should be that IndexNames starting with . are deprecated?
        Right(())
      } else
        Right(())
    }
  }
  type IndexName = IndexName.Type

}
