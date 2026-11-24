package zio.elasticsearch

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.query.Match
import zio.json.ast.Json.Str
import zio.test.Assertion.equalTo
import zio.test._

import java.time.{Instant, LocalDate, LocalDateTime, OffsetDateTime, ZoneId, ZoneOffset, ZonedDateTime}
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
      },
      suite("encoding date and time values")(
        test("Instant") {
          assert(Instant.parse("2024-01-01T10:00:00Z").toJson)(equalTo(Str("2024-01-01T10:00:00Z")))
        },
        test("LocalDate") {
          assert(LocalDate.of(2024, 1, 1).toJson)(equalTo(Str("2024-01-01")))
        },
        test("LocalDateTime") {
          assert(LocalDateTime.of(2024, 1, 1, 10, 0).toJson)(equalTo(Str("2024-01-01T10:00:00")))
        },
        test("OffsetDateTime") {
          assert(OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.ofHours(2)).toJson)(
            equalTo(Str("2024-01-01T10:00:00+02:00"))
          )
        },
        test("ZonedDateTime") {
          assert(ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneId.of("Europe/Belgrade")).toJson)(
            equalTo(Str("2024-01-01T10:00:00+01:00"))
          )
        }
      )
    )

  private val FieldName = "fieldName"
}
