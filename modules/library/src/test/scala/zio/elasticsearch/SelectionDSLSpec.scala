package zio.elasticsearch

import zio.schema.{DeriveSchema, Schema}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object SelectionDSLSpec extends ZIOSpecDefault {

  final case class Address(street: String, number: Int)

  object Address {

    implicit val schema: Schema.CaseClass2[String, Int, Address] = DeriveSchema.gen[Address]

    val (street, number) = schema.makeAccessors(ElasticQueryAccessorBuilder)
  }

  final case class Student(name: String, address: Address)

  object Student {

    implicit val schema: Schema.CaseClass2[String, Address, Student] = DeriveSchema.gen[Student]

    val (name, address) = schema.makeAccessors(ElasticQueryAccessorBuilder)
  }

  override def spec: Spec[TestEnvironment, Any] =
    suite("Selection DSL")(
      test("properly encode single field path")(
        assertTrue(Field(None, "name").toString == "name")
      ),
      test("properly encode single field path using accessor")(
        assertTrue(Student.name.toString == "name")
      ),
      test("properly encode nested field path")(
        assertTrue(Field(Some(Field(None, "address")), "number").toString == "address.number")
      ),
      test("properly encode nested field path using accessors")(
        assertTrue((Student.address / Address.number).toString == "address.number")
      )
    )
}
