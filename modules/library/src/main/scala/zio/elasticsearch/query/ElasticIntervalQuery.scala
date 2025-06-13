package zio.elasticsearch.query

import zio.Chunk
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj, Str}

sealed trait IntervalRule {
  private[elasticsearch] def toJson: Json
}

private[elasticsearch] final case class IntervalAllOf[S](
  intervals: Chunk[IntervalRule],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule {
  private[elasticsearch] def toJson: Json =
    Obj(
      "all_of" -> Obj(
        Chunk(
          Some("intervals" -> Arr(intervals.map(_.toJson): _*)),
          maxGaps.map("max_gaps" -> _.toJson),
          ordered.map("ordered" -> _.toJson),
          filter.map("filter" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalAnyOf[S](
  intervals: Chunk[IntervalRule],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule {
  private[elasticsearch] def toJson: Json =
    Obj(
      "any_of" -> Obj(
        "intervals" -> Arr(intervals.map(_.toJson): _*)
      )
    )
}

private[elasticsearch] final case class IntervalFilter[S](
  after: Option[IntervalRule] = None,
  before: Option[IntervalRule] = None,
  containedBy: Option[IntervalRule] = None,
  containing: Option[IntervalRule] = None,
  notContainedBy: Option[IntervalRule] = None,
  notContaining: Option[IntervalRule] = None,
  notOverlapping: Option[IntervalRule] = None,
  overlapping: Option[IntervalRule] = None,
  script: Option[Json] = None
) {
  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        after.map("after" -> _.toJson),
        before.map("before" -> _.toJson),
        containedBy.map("contained_by" -> _.toJson),
        containing.map("containing" -> _.toJson),
        notContainedBy.map("not_contained_by" -> _.toJson),
        notContaining.map("not_containing" -> _.toJson),
        notOverlapping.map("not_overlapping" -> _.toJson),
        overlapping.map("overlapping" -> _.toJson),
        script.map("script" -> _)
      ).flatten: _*
    )
}

private[elasticsearch] final case class IntervalFuzzy[S](
  term: String,
  prefixLength: Option[Int],
  transpositions: Option[Boolean],
  fuzziness: Option[String],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {
  private[elasticsearch] def toJson: Json =
    Obj(
      "fuzzy" -> Obj(
        Chunk(
          Some("term" -> term.toJson),
          prefixLength.map("prefix_length" -> _.toJson),
          transpositions.map("transpositions" -> _.toJson),
          fuzziness.map("fuzziness" -> _.toJson),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalMatch[S](
  query: String,
  analyzer: Option[String],
  useField: Option[String],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule { self =>

  def withAnalyzer(a: String)          = copy(analyzer = Some(a))
  def withUseField(f: String)          = copy(useField = Some(f))
  def withMaxGaps(g: Int)              = copy(maxGaps = Some(g))
  def withOrdered(o: Boolean)          = copy(ordered = Some(o))
  def withFilter(f: IntervalFilter[S]) = copy(filter = Some(f))

  override private[elasticsearch] def toJson: Json =
    Obj(
      "match" -> Obj(
        Chunk(
          Some("query" -> Str(query)),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson),
          maxGaps.map("max_gaps" -> _.toJson),
          ordered.map("ordered" -> _.toJson),
          filter.map("filter" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalPrefix(
  prefix: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {

  def withAnalyzer(a: String) = copy(analyzer = Some(a))
  def withUseField(f: String) = copy(useField = Some(f))

  override private[elasticsearch] def toJson: Json =
    Obj(
      "prefix" -> Obj(
        Chunk(
          Some("prefix" -> Str(prefix)),
          analyzer.map(a => "analyzer" -> Str(a)),
          useField.map(u => "use_field" -> Str(u))
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalRange[S](
  lower: Option[Either[String, String]],
  upper: Option[Either[String, String]],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {
  private[elasticsearch] def toJson: Json = {
    val lowerJson = lower.map {
      case Left(gt)   => "gt"  -> gt.toJson
      case Right(gte) => "gte" -> gte.toJson
    }
    val upperJson = upper.map {
      case Left(lt)   => "lt"  -> lt.toJson
      case Right(lte) => "lte" -> lte.toJson
    }

    Obj(
      "range" -> Obj(
        Chunk(
          lowerJson,
          upperJson,
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
  }
}

private[elasticsearch] final case class IntervalRegexp[S](
  pattern: Regexp[S],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {
  private[elasticsearch] def toJson: Json =
    Obj(
      "regexp" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson(None)),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalWildcard[S](
  pattern: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {

  def withAnalyzer(a: String): IntervalWildcard[S] =
    copy(analyzer = Some(a))

  def withUseField(f: String): IntervalWildcard[S] =
    copy(useField = Some(f))

  private[elasticsearch] def toJson: Json =
    Obj(
      "wildcard" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}
