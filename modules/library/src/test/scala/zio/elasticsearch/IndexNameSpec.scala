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
        val name = "Index-name"
        assert(IndexName.make(name))(equalTo(Validation.fail(indexNameFailureMessage(name))))
      },
      test("fail for string containing charachter '*'") {
        val name = "index*name"
        assert(IndexName.make(name))(equalTo(Validation.fail(indexNameFailureMessage(name))))
      },
      test("fail for string containing charachter ':'") {
        val name = "index:name"
        assert(IndexName.make(name))(equalTo(Validation.fail(indexNameFailureMessage(name))))
      },
      test("fail for string starting with charachter '-'") {
        val name = "-index.name"
        assert(IndexName.make(name))(equalTo(Validation.fail(indexNameFailureMessage(name))))
      },
      test("fail for string '.'") {
        val name = "."
        assert(IndexName.make(name))(equalTo(Validation.fail(indexNameFailureMessage(name))))
      },
      test("fail for string longer than 255 bytes") {
        check(Gen.stringN(256)(Gen.alphaChar)) { name =>
          val lowerCaseStr = name.toLowerCase()
          assert(IndexName.make(lowerCaseStr.toLowerCase))(
            equalTo(Validation.fail(indexNameFailureMessage(lowerCaseStr.toLowerCase)))
          )
        }
      }
    )

  private def indexNameFailureMessage(name: String): String =
    s"""$name did not satisfy 
       |   - Must be lower case only
       |   - Cannot include \\, /, *, ?, ", <, >, |, ` `(space character), `,`(comma), #.
       |   - Cannot include ":"(since 7.0).
       |   - Cannot start with -, _, +.
       |   - Cannot be `.` or `..`.
       |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
       |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
       |""".stripMargin
}
