package zio.elasticsearch

import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.query.Match
import zio.test.Assertion.equalTo
import zio.test._

import java.util.UUID

object ElasticPrimitivesSpec extends ZIOSpecDefault {

  private val fieldName = "fieldName"

  override def spec: Spec[TestEnvironment, Any] =
    suite("Elastic Primitives")(
      test("successfully create matches query with String") {
        assert(matches(fieldName, "string"))(equalTo(Match[Any, String](fieldName, "string", boost = None)))
      },
      test("successfully create matches query with BigDecimal") {
        assert(matches(fieldName, BigDecimal(1)))(
          equalTo(Match[Any, BigDecimal](fieldName, BigDecimal(1), boost = None))
        )
      },
      test("successfully create matches query with Boolean") {
        assert(matches(fieldName, true))(equalTo(Match[Any, Boolean](fieldName, true, boost = None)))
      },
      test("successfully create matches query with Double") {
        assert(matches(fieldName, 1.00))(equalTo(Match[Any, Double](fieldName, 1.00, boost = None)))
      },
      test("successfully create matches query with Int") {
        assert(matches(fieldName, 1))(equalTo(Match[Any, Int](fieldName, 1, boost = None)))
      },
      test("successfully create matches query with Long") {
        assert(matches(fieldName, 1L))(equalTo(Match[Any, Long](fieldName, 1L, boost = None)))
      },
      test("successfully create matches query with UUID") {
        val uuid = UUID.randomUUID()
        assert(matches(fieldName, uuid))(equalTo(Match[Any, UUID](fieldName, uuid, boost = None)))
      }
    )
}
