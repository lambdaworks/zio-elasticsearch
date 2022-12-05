package zio.elasticsearch

import zio.schema.{DeriveSchema, Schema}

final case class CustomerDocument(id: String, name: String, address: String, balance: BigDecimal)

final case class EmployeeDocument(id: String, name: String, degree: String)

object CustomerDocument {
  implicit val schema: Schema[CustomerDocument] = DeriveSchema.gen[CustomerDocument]
}

object EmployeeDocument {
  implicit val schema: Schema[EmployeeDocument] = DeriveSchema.gen[EmployeeDocument]
}
