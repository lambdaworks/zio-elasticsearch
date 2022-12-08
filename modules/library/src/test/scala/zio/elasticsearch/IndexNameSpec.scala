package zio.elasticsearch

import zio.prelude.Validation
import zio.test.Assertion.equalTo
import zio.test._

object IndexNameSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] =
    suite("IndexName validation")(
      test("succeed for valid string") {
        assert(IndexName.make("index-name"))(equalTo(Validation.succeed(IndexName("index-name"))))
      },
      test("fail for string containing upper letter") {
        val invalidIndexStr = "Index-name"
        assert(IndexName.make(invalidIndexStr))(equalTo(Validation.fail(validationFailedStr(invalidIndexStr))))
      },
      test("fail for string containing charachter '*'") {
        val invalidIndexStr = "index*name"
        assert(IndexName.make(invalidIndexStr))(equalTo(Validation.fail(validationFailedStr(invalidIndexStr))))
      },
      test("fail for string containing charachter ':'") {
        val invalidIndexStr = "index:name"
        assert(IndexName.make(invalidIndexStr))(equalTo(Validation.fail(validationFailedStr(invalidIndexStr))))
      },
      test("fail for string starting with charachter '-'") {
        val invalidIndexStr = "-index.name"
        assert(IndexName.make(invalidIndexStr))(equalTo(Validation.fail(validationFailedStr(invalidIndexStr))))
      },
      test("fail for string '.'") {
        val invalidIndexStr = "."
        assert(IndexName.make(invalidIndexStr))(equalTo(Validation.fail(validationFailedStr(invalidIndexStr))))
      },
      test("fail for string longer than 255 bytes") {
        checkN(5)(Gen.stringN(256)(Gen.alphaChar)) { invalidIndexStr =>
          val lowerCaseStr = invalidIndexStr.toLowerCase()
          assert(IndexName.make(lowerCaseStr.toLowerCase))(
            equalTo(Validation.fail(validationFailedStr(lowerCaseStr.toLowerCase)))
          )
        }
      }
    )

  private def validationFailedStr(indexStr: String): String =
    s"""$indexStr did not satisfy 
       |   - Must be lower case only
       |   - Cannot include \\, /, *, ?, ", <, >, |, ` `(space character), `,`(comma), #.
       |   - Cannot include ":"(since 7.0).
       |   - Cannot start with -, _, +.
       |   - Cannot be `.` or `..`.
       |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
       |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
       |""".stripMargin
}
