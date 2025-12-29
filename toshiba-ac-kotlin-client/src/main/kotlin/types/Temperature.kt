package toshibaac.client.types

@JvmInline
public value class Temperature(public val value: Int) {
    internal companion object {
        fun fromRaw(raw: String?): Temperature? = raw?.let { fromRaw(it.toUByte(16).toByte()) }

        fun fromRaw(raw: Byte): Temperature? = if (raw == 126.toByte()) {
            Temperature(-1)
        } else if (raw == (-128).toByte() || raw == 127.toByte() || raw == (-1).toByte()) {
            null
        } else {
            Temperature(raw.toInt())
        }
    }

    init {
        require(value in -127..125) { "Temperature must be in range -127..125" }
    }
}

internal val Temperature?.asByte
    get() = when (this) {
        null -> 0xff.toByte()
        Temperature(-1) -> 126.toByte()
        else -> value.toByte()
    }
