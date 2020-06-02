package tech.pegasys.teku.phase1.ssz

@ExperimentalUnsignedTypes
public inline class UInt64(val v: ULong) : SSZObject, Comparable<UInt64> {

  override fun hash_tree_root(): org.apache.tuweni.bytes.Bytes32 {
    TODO("Not yet implemented")
  }

  companion object {
    public val MIN_VALUE: UInt64 = UInt64(0uL)
    public val MAX_VALUE: UInt64 = UInt64((-1).toULong())
    public const val SIZE_BYTES: Int = 8
    public const val SIZE_BITS: Int = 64
  }

  @Suppress("OVERRIDE_BY_INLINE")
  public override inline operator fun compareTo(other: UInt64): Int = this.v.compareTo(other.v)
  public inline operator fun compareTo(other: UByte): Int = v.compareTo(other.toULong())
  public inline operator fun compareTo(other: UShort): Int = v.compareTo(other.toULong())
  public inline operator fun compareTo(other: UInt): Int = v.compareTo(other.toULong())

  public inline operator fun plus(other: UInt64): UInt64 = UInt64(this.v.plus(other.v))
  public inline operator fun plus(other: ULong): UInt64 = UInt64(this.v.plus(other))
  public inline operator fun plus(other: UByte): UInt64 = this.plus(other.toULong())
  public inline operator fun plus(other: UShort): UInt64 = this.plus(other.toULong())
  public inline operator fun plus(other: UInt): UInt64 = this.plus(other.toULong())

  public inline operator fun minus(other: UInt64): UInt64 = UInt64(this.v.minus(other.v))
  public inline operator fun minus(other: ULong): UInt64 = UInt64(this.v.minus(other))
  public inline operator fun minus(other: UByte): UInt64 = this.minus(other.toULong())
  public inline operator fun minus(other: UShort): UInt64 = this.minus(other.toULong())
  public inline operator fun minus(other: UInt): UInt64 = this.minus(other.toULong())

  public inline operator fun times(other: UInt64): UInt64 = UInt64(this.v.times(other.v))
  public inline operator fun times(other: ULong): UInt64 = UInt64(this.v.times(other))
  public inline operator fun times(other: UByte): UInt64 = this.times(other.toULong())
  public inline operator fun times(other: UShort): UInt64 = this.times(other.toULong())
  public inline operator fun times(other: UInt): UInt64 = this.times(other.toULong())

  public inline operator fun div(other: ULong): UInt64 = UInt64(v.div(other))
  public inline operator fun div(other: UInt64): UInt64 = UInt64(v.div(other.v))
  public inline operator fun div(other: UByte): UInt64 = this.div(other.toULong())
  public inline operator fun div(other: UShort): UInt64 = this.div(other.toULong())
  public inline operator fun div(other: UInt): UInt64 = this.div(other.toULong())

  public inline operator fun rem(other: UByte): UInt64 = this.rem(other.toULong())
  public inline operator fun rem(other: UShort): UInt64 = this.rem(other.toULong())
  public inline operator fun rem(other: UInt): UInt64 = this.rem(other.toULong())
  public inline operator fun rem(other: ULong): UInt64 = UInt64(v.rem(other))
  public inline operator fun rem(other: UInt64): UInt64 = this.rem(other.v)

  public inline operator fun inc(): UInt64 = UInt64(v.inc())
  public inline operator fun dec(): UInt64 = UInt64(v.dec())

  public inline operator fun rangeTo(other: UInt64): UInt64Range = UInt64Range(this, other)
  public inline operator fun rangeTo(other: ULong): UInt64Range = UInt64Range(this, UInt64(other))

  public inline infix fun shl(bitCount: Int): UInt64 = UInt64(v shl bitCount)
  public inline infix fun shr(bitCount: Int): UInt64 = UInt64(v shr bitCount)
  public inline infix fun and(other: ULong): UInt64 = UInt64(this.v and v)
  public inline infix fun or(other: ULong): UInt64 = UInt64(this.v or v)
  public inline infix fun xor(other: ULong): UInt64 = UInt64(this.v xor v)
  public inline infix fun and(other: UInt64): UInt64 = UInt64(this.v and other.v)
  public inline infix fun or(other: UInt64): UInt64 = UInt64(this.v or other.v)
  public inline infix fun xor(other: UInt64): UInt64 = UInt64(this.v xor other.v)
  public inline fun inv(): UInt64 = UInt64(v.inv())

  public inline fun toByte(): Byte = v.toByte()
  public inline fun toShort(): Short = v.toShort()
  public inline fun toInt(): Int = v.toInt()
  public inline fun toLong(): Long = v.toLong()
  public inline fun toUByte(): UByte = v.toUByte()
  public inline fun toUShort(): UShort = v.toUShort()
  public inline fun toUInt(): UInt = v.toUInt()
  public inline fun toULong(): ULong = v
  public inline fun toFloat(): Float = v.toDouble().toFloat()
  public inline fun toDouble(): Double = v.toDouble()

  public override fun toString(): String = v.toString()
}

@ExperimentalUnsignedTypes
inline fun Byte.toUInt64(): UInt64 = UInt64(this.toULong())
@ExperimentalUnsignedTypes
inline fun Short.toUInt64(): UInt64 = UInt64(this.toULong())
@ExperimentalUnsignedTypes
inline fun Int.toUInt64(): UInt64 = UInt64(this.toULong())
@ExperimentalUnsignedTypes
inline fun Long.toUInt64(): UInt64 = UInt64(this.toULong())
@ExperimentalUnsignedTypes
inline fun Float.toUInt64(): UInt64 = UInt64(this.toULong())
@ExperimentalUnsignedTypes
inline fun Double.toUInt64(): UInt64 = UInt64(this.toULong())

@ExperimentalUnsignedTypes
public infix fun UInt64.until(to: UInt64): UInt64Range {
  if (to <= UInt64.MIN_VALUE) return UInt64Range.EMPTY
  return this .. (to - 1u).toULong()
}

@ExperimentalUnsignedTypes
public class UInt64Range(start: UInt64, endInclusive: UInt64) : UInt64Progression(start, endInclusive, 1), ClosedRange<UInt64> {
  override val start: UInt64 get() = first
  override val endInclusive: UInt64 get() = last

  override fun contains(value: UInt64): Boolean = first <= value && value <= last

  override fun isEmpty(): Boolean = first > last

  override fun equals(other: Any?): Boolean =
      other is UInt64Range && (isEmpty() && other.isEmpty() ||
          first == other.first && last == other.last)

  override fun hashCode(): Int =
      if (isEmpty()) -1 else (31 * (first xor (first shr 32)).toInt() + (last xor (last shr 32)).toInt())

  override fun toString(): String = "$first..$last"

  companion object {
    /** An empty range of values of type ULong. */
    public val EMPTY: UInt64Range = UInt64Range(UInt64.MAX_VALUE, UInt64.MIN_VALUE)
  }
}

@ExperimentalUnsignedTypes
private fun differenceModulo(a: UInt64, b: UInt64, c: UInt64): UInt64 {
  val ac = a % c
  val bc = b % c
  return if (ac >= bc) ac - bc else ac - bc + c
}

@ExperimentalUnsignedTypes
fun getProgressionLastElement(start: UInt64, end: UInt64, step: Long): UInt64 = when {
  step > 0 -> if (start >= end) end else end - differenceModulo(end, start, step.toUInt64())
  step < 0 -> if (start <= end) end else end + differenceModulo(start, end, (-step).toUInt64())
  else -> throw kotlin.IllegalArgumentException("Step is zero.")
}

@ExperimentalUnsignedTypes
public open class UInt64Progression
internal constructor(
    start: UInt64,
    endInclusive: UInt64,
    step: Long
) : Iterable<UInt64> {
  init {
    if (step == 0.toLong()) throw kotlin.IllegalArgumentException("Step must be non-zero.")
    if (step == Long.MIN_VALUE) throw kotlin.IllegalArgumentException("Step must be greater than Long.MIN_VALUE to avoid overflow on negation.")
  }

  val first: UInt64 = start
  val last: UInt64 = getProgressionLastElement(start, endInclusive, step)
  val step: Long = step

  override fun iterator(): UInt64Iterator = UInt64ProgressionIterator(first, last, step)

  /** Checks if the progression is empty. */
  public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

  override fun equals(other: Any?): Boolean =
      other is UInt64Progression && (isEmpty() && other.isEmpty() ||
          first == other.first && last == other.last && step == other.step)

  override fun hashCode(): Int =
      if (isEmpty()) -1 else (31 * (31 * (first xor (first shr 32)).toInt() + (last xor (last shr 32)).toInt()) + (step xor (step ushr 32)).toInt())

  override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

  companion object {
    public fun fromClosedRange(rangeStart: UInt64, rangeEnd: UInt64, step: Long): UInt64Progression = UInt64Progression(rangeStart, rangeEnd, step)
  }
}

@ExperimentalUnsignedTypes
public abstract class UInt64Iterator : Iterator<UInt64> {
  override final fun next() = nextUInt64()
  public abstract fun nextUInt64(): UInt64
}

@ExperimentalUnsignedTypes
private class UInt64ProgressionIterator(first: UInt64, last: UInt64, step: Long) : UInt64Iterator() {
  private val finalElement = last
  private var hasNext: Boolean = if (step > 0) first <= last else first >= last
  private val step = step.toULong() // use 2-complement math for negative steps
  private var next = if (hasNext) first else finalElement

  override fun hasNext(): Boolean = hasNext

  override fun nextUInt64(): UInt64 {
    val value = next
    if (value == finalElement) {
      if (!hasNext) throw kotlin.NoSuchElementException()
      hasNext = false
    } else {
      next += step
    }
    return value
  }
}
