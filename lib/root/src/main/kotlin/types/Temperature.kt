package toshibaac.client.types

@JvmInline
public value class Temperature(public val value: Int) {
    internal companion object {
        fun fromRaw(raw: String?): Temperature? = raw?.let { fromRaw(it.toUByte(16).toByte()) }

        fun fromRaw(raw: Byte): Temperature? = when (raw) {
            126.toByte() -> Temperature(-1)
            (-128).toByte(), 127.toByte(), (-1).toByte() -> null
            else -> Temperature(raw.toInt())
        }
    }

    init {
        require(value in -127..125) { "Temperature must be in range -127..125" }
    }
}

private const val HEATING_8C_OFFSET = 16

/** With merit A [MeritAMode.HEATING_8C], the AC transfers the target temperature offset by 16. */
internal fun Temperature.fromHeating8CRaw(meritByte: Byte): Temperature = if ((meritByte.toInt() and 0x0F) == 0x04 && value > HEATING_8C_OFFSET) {
    Temperature(value - HEATING_8C_OFFSET)
} else {
    this
}

internal fun Temperature.toHeating8CRaw(meritAMode: MeritAMode?): Temperature = when (meritAMode) {
    MeritAMode.HEATING_8C -> Temperature(value + HEATING_8C_OFFSET)
    else -> this
}

internal val Temperature?.asByte
    get() = when (this) {
        null -> 0xff.toByte()
        Temperature(-1) -> 126.toByte()
        else -> value.toByte()
    }
