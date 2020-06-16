package tech.pegasys.teku.phase1.integration.datastructures

internal typealias Callback<T> = (T) -> Unit

internal abstract class Mutable<T>(internal var callback: Callback<T>?) {
  protected inline fun onUpdate(value: T) = callback?.invoke(value)
}
