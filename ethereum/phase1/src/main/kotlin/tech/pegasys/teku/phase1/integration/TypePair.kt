package tech.pegasys.teku.phase1.integration

import tech.pegasys.teku.phase1.integration.datastructures.Callback
import tech.pegasys.teku.phase1.integration.datastructures.Mutable
import tech.pegasys.teku.phase1.integration.datastructures.Wrapper
import tech.pegasys.teku.phase1.onotole.ssz.SSZComposite
import kotlin.reflect.KClass

internal interface TypePair<Onotole : Any, Teku : Any> {
  val teku: KClass<Teku>
  val onotole: KClass<Onotole>

  fun wrap(v: Teku): Onotole
  fun unwrap(v: Onotole): Teku

  fun toStringHelper(): String {
    return "TypePair(onotole=$onotole, teku=$teku)"
  }
}

internal interface SSZCompositeType<Onotole : SSZComposite, Teku : Any> : TypePair<Onotole, Teku>

internal abstract class WrappedTypePair<Onotole : SSZComposite, TWrapper : Wrapper<Teku>, Teku : Any>(
  override val onotole: KClass<Onotole>,
  override val teku: KClass<Teku>,
  private val wrapper: KClass<TWrapper>
) : SSZCompositeType<Onotole, Teku> {
  override fun wrap(v: Teku): Onotole {
    return wrapper.java.getConstructor(teku.java).newInstance(v) as Onotole
  }

  fun wrap(v: Teku, onUpdate: Callback<Onotole>): Onotole {
    val wrapped: Onotole = wrap(v)
    if (wrapped is Mutable<*>) {
      (wrapped as Mutable<Onotole>).callback = onUpdate
    }
    return wrapped
  }

  override fun unwrap(v: Onotole) = (v as TWrapper).v

  override fun toString() = toStringHelper()
}
