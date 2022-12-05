package zio.elasticsearch

import zio.schema.{DeriveSchema, Schema}

final case class UserDocument1(id: String, name: String, count: Int)

final case class UserDocument2(desc: String)

object UserDocument {
  implicit val schema1: Schema[UserDocument1] = DeriveSchema.gen[UserDocument1]
  implicit val schema2: Schema[UserDocument2] = DeriveSchema.gen[UserDocument2]
}
