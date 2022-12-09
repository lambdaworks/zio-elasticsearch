package zio

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils._
import zio.prelude.Assertion.isEmptyString
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
      if (
        name.toLowerCase != name ||
        startsWithAny(name, "+", "-", "_") ||
        containsAny(name, List("*", "?", "\"", "<", ">", "|", " ", ",", "#", ":")) ||
        equalsAny(name, ".", "..") ||
        name.getBytes().length > 255
      )
        Left(
          failure(
            s"""
               |   - Must be lower case only
               |   - Cannot include \\, /, *, ?, ", <, >, |, ` `(space character), `,`(comma), #.
               |   - Cannot include ":"(since 7.0).
               |   - Cannot be empty
               |   - Cannot start with -, _, +.
               |   - Cannot be `.` or `..`.
               |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
               |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
               |""".stripMargin
          )
        )
      else
        Right(())
    }
  }
  type IndexName = IndexName.Type

  def containsAny(name: String, params: List[String]): Boolean =
    params.exists(StringUtils.contains(name, _))

}
