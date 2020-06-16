package tech.pegasys.teku.phase1.integration

import kotlin.reflect.KClass

interface TypePair<Onotole : Any, Teku : Any> {
  val teku: KClass<Teku>
  val onotole: KClass<Onotole>

  fun wrap(v: Teku): Onotole
  fun unwrap(v: Onotole): Teku
}