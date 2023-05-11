package zio.elasticsearch

import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.query.Match
import zio.test.Assertion.equalTo
import zio.test._

import java.util.UUID

object ElasticPrimitiveSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("ElasticPrimitive")(
      test("BigDecimal") {
        assert(matches(FieldName, BigDecimal(1)))(
          equalTo(Match[Any, BigDecimal](FieldName, BigDecimal(1)))
        )
      },
      test("Boolean") {
        assert(matches(FieldName, true))(equalTo(Match[Any, Boolean](FieldName, true)))
      },
      test("Double") {
        assert(matches(FieldName, 1.00))(equalTo(Match[Any, Double](FieldName, 1.00)))
      },
      test("Int") {
        assert(matches(FieldName, 1))(equalTo(Match[Any, Int](FieldName, 1)))
      },
      test("Long") {
        assert(matches(FieldName, 1L))(equalTo(Match[Any, Long](FieldName, 1L)))
      },
      test("String") {
        assert(matches(FieldName, "string"))(equalTo(Match[Any, String](FieldName, "string")))
      },
      test("UUID") {
        val uuid = UUID.randomUUID()
        assert(matches(FieldName, uuid))(equalTo(Match[Any, UUID](FieldName, uuid)))
      }
    )

  private val FieldName = "fieldName"
}
