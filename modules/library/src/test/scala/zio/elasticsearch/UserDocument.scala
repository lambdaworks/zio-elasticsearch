package zio.elasticsearch

import zio.schema.{DeriveSchema, Schema}

case class UserDocument(id: String, name: String, address: String, balance: Double, age: Int)

object UserDocument {
  implicit val schema: Schema[UserDocument] = DeriveSchema.gen[UserDocument]
}
