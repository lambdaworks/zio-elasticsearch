package zio.elasticsearch.utils

import zio.prelude.{Newtype, NewtypeCustom}

abstract class UnsafeWrapUtil {
  def unsafeWrap[A, T <: Newtype[A] | NewtypeCustom[A]](value: A)(newtype: T): newtype.Type = newtype.wrap(value)
}
