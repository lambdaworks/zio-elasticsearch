package zio.elasticsearch.request.options

import zio.schema.Schema

trait HasSourceFiltering[R <: HasSourceFiltering[R]] {
  def excludes(fields: String*): R

  def includes(fields: String*): R

  final def includes(schema: Schema.Record[_]): R = {
    def loop(schema: Schema.Record[_], prefix: Option[String]): List[String] =
      schema.fields.toList.flatMap { field =>
        Schema.force(field.schema) match {
          case schema: Schema.Record[_] => loop(schema, prefix.map(_ + "." + field.name).orElse(Some(field.name)))
          case schema: Schema.Sequence[_, _, _] =>
            Schema.force(schema.elementSchema) match {
              case schema: Schema.Record[_] => loop(schema, prefix.map(_ + "." + field.name).orElse(Some(field.name)))
              case s                        => List(prefix.fold[String](field.name)(_ + "." + field.name))
            }
          case s => List(prefix.fold[String](field.name)(_ + "." + field.name))
        }
      }

    includes(loop(schema, None): _*)
  }
}
