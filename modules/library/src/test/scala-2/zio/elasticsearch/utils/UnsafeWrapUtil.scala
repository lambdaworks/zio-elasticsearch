package zio.elasticsearch.utils

import zio.prelude.Newtype

abstract class UnsafeWrapUtil {
  def unsafeWrap[A, T <: Newtype[A]](value: A)(newtype: T): newtype.Type = Newtype.unsafeWrap(newtype)(value)
}
