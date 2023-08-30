package zio.elasticsearch

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.{equalsAny, startsWithAny}
import zio.Chunk
import zio.prelude.{AssertionError, Validator}

object IndexPatternValidator
  extends Validator[String](pattern => {
    def containsAny(string: String, params: Chunk[String]): Boolean =
      params.exists(StringUtils.contains(string, _))

    def isValid(pattern: String): Boolean =
      pattern.toLowerCase == pattern &&
        !startsWithAny(pattern, "+") &&
        pattern.nonEmpty &&
        !containsAny(string = pattern, params = Chunk("?", "\"", "<", ">", "|", " ", ",", "#", ":")) &&
        !equalsAny(pattern, ".", "..") &&
        pattern.getBytes().length <= 255

    if (isValid(pattern)) {
      Right(())
    } else {
      Left(
        AssertionError.Failure(
          s"""
             |   - Must be lower case only
             |   - Cannot include \\, /, ?, ", <, >, |, ` `(space character), `,`(comma), #.
             |   - Cannot include ":"(since 7.0).
             |   - Cannot be empty
             |   - Cannot start with +.
             |   - Cannot be `.` or `..`.
             |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
             |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
             |""".stripMargin
        )
      )
    }
  })