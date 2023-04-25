package zio.elasticsearch

import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.query.Match
import zio.test.Assertion.equalTo
import zio.test._

import java.util.UUID

object ElasticPrimitiveSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("Elastic Primitives")(
      test("successfully create matches query with String") {
        assert(matches(FieldName, "string"))(equalTo(Match[Any, String](FieldName, "string", boost = None)))
      },
      test("successfully create matches query with BigDecimal") {
        assert(matches(FieldName, BigDecimal(1)))(
          equalTo(Match[Any, BigDecimal](FieldName, BigDecimal(1), boost = None))
        )
      },
      test("successfully create matches query with Boolean") {
        assert(matches(FieldName, true))(equalTo(Match[Any, Boolean](FieldName, true, boost = None)))
      },
      test("successfully create matches query with Double") {
        assert(matches(FieldName, 1.00))(equalTo(Match[Any, Double](FieldName, 1.00, boost = None)))
      },
      test("successfully create matches query with Int") {
        assert(matches(FieldName, 1))(equalTo(Match[Any, Int](FieldName, 1, boost = None)))
      },
      test("successfully create matches query with Long") {
        assert(matches(FieldName, 1L))(equalTo(Match[Any, Long](FieldName, 1L, boost = None)))
      },
      test("successfully create matches query with UUID") {
        val uuid = UUID.randomUUID()
        assert(matches(FieldName, uuid))(equalTo(Match[Any, UUID](FieldName, uuid, boost = None)))
      }
    )

  private val FieldName = "fieldName"
}
