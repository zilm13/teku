package tech.pegasys.teku.phase1.integration

import tech.pegasys.teku.phase1.integration.datastructures.Callback
import tech.pegasys.teku.phase1.integration.datastructures.Mutable
import tech.pegasys.teku.phase1.integration.datastructures.Wrapper
import kotlin.reflect.KClass

interface TypePair<Onotole : Any, Teku : Any> {
  val teku: KClass<Teku>
  val onotole: KClass<Onotole>

  fun wrap(v: Teku): Onotole
  fun unwrap(v: Onotole): Teku
}

internal abstract class WrappedTypePair<Onotole : Any, TWrapper : Wrapper<Teku>, Teku : Any>(
  override val onotole: KClass<Onotole>,
  override val teku: KClass<Teku>
) : TypePair<Onotole, Teku> {
  override fun wrap(v: Teku): Onotole {
    TODO("Implement via reflection")
  }

  fun wrap(v: Teku, onUpdate: Callback<Onotole>): Onotole {
    val wrapped: Onotole = wrap(v)
    if (wrapped is Mutable<*>) {
      (wrapped as Mutable<Onotole>).callback = onUpdate
    }
    return wrapped
  }

  override fun unwrap(v: Onotole) = (v as TWrapper).v
}