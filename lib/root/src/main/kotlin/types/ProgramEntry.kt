@file:OptIn(ExperimentalStdlibApi::class)

package toshibaac.client.types

public data class ProgramEntry(
    public val hours: Hours,
    public val minutes: Minutes,
    public val acStatus: ACStatus,
    public val acMode: ACMode?,
    public val temperature: TargetTemperature?,
    public val fanMode: FanMode?,
    public val meritBMode: MeritBMode?,
    public val meritAMode: MeritAMode?,
    public val swingMode: SwingMode?,
) {
    internal companion object {
        fun from(str: String): ProgramEntry? {
            if (str.isEmpty() || str == "invalid") { // TODO: better representation of invalid?
                return null
            }
            val bytes = str.hexToByteArray()
            return ProgramEntry(
                hours = Hours(bytes[0].toHexString().toInt(10)),
                minutes = Minutes(bytes[1].toHexString().toInt(10)),
                acStatus = when (bytes[2]) {
                    0x30.toByte() -> ACStatus.ON
                    0x31.toByte() -> ACStatus.OFF
                    else -> throw IllegalArgumentException("Invalid ACStatus byte: ${bytes[0]}")
                },
                acMode = when (bytes[3]) {
                    0x41.toByte() -> ACMode.AUTO
                    0x42.toByte() -> ACMode.COOL
                    0x43.toByte() -> ACMode.HEAT
                    0x44.toByte() -> ACMode.DRY
                    0x45.toByte() -> ACMode.FAN
                    0xFF.toByte() -> null
                    0x00.toByte() -> null // TODO: how to represent better?
                    else -> throw IllegalArgumentException("Invalid ACMode byte: ${bytes[1]}")
                },
                temperature = Temperature.fromRaw(bytes[4])?.let { TargetTemperature(it) },
                fanMode = when (bytes[5]) {
                    0x41.toByte() -> FanMode.AUTO
                    0x31.toByte() -> FanMode.QUIET
                    0x32.toByte() -> FanMode.LOW
                    0x33.toByte() -> FanMode.MEDIUM_LOW
                    0x34.toByte() -> FanMode.MEDIUM
                    0x35.toByte() -> FanMode.MEDIUM_HIGH
                    0x36.toByte() -> FanMode.HIGH
                    0x00.toByte() -> null // TODO: how to represent better?
                    0xFF.toByte() -> null
                    else -> throw IllegalArgumentException("Invalid FanMode byte: ${bytes[3]}")
                },
                meritBMode = when (val halfByte = (bytes[6].toUInt() shr 4).toByte()) {
                    0x03.toByte() -> MeritBMode.FIREPLACE_1
                    0x02.toByte() -> MeritBMode.FIREPLACE_2
                    0x01.toByte() -> MeritBMode.OFF
                    0x00.toByte() -> MeritBMode.OFF
                    (-1).toByte() -> null
                    else -> throw IllegalArgumentException("Invalid MeritBMode half-byte: $halfByte")
                },
                meritAMode = when (val halfByte = bytes[6].toUInt() and 0x0Fu) {
                    0x01u -> MeritAMode.HIGH_POWER
                    0x02u -> MeritAMode.CDU_SILENT_1
                    0x03u -> MeritAMode.ECO
                    0x04u -> MeritAMode.HEATING_8C
                    0x05u -> MeritAMode.SLEEP_CARE
                    0x06u -> MeritAMode.FLOOR
                    0x07u -> MeritAMode.COMFORT
                    0x0Au -> MeritAMode.CDU_SILENT_2
                    0x00u -> MeritAMode.OFF
                    0x0Fu -> null
                    else -> throw IllegalArgumentException("Invalid MeritAMode half-byte: $halfByte")
                },
                swingMode = when (bytes[7]) {
                    0x31.toByte() -> SwingMode.OFF
                    0x41.toByte() -> SwingMode.VERTICAL
                    0x42.toByte() -> SwingMode.HORIZONTAL
                    0x43.toByte() -> SwingMode.BOTH
                    0x50.toByte() -> SwingMode.FIXED_1
                    0x51.toByte() -> SwingMode.FIXED_2
                    0x52.toByte() -> SwingMode.FIXED_3
                    0x53.toByte() -> SwingMode.FIXED_4
                    0x54.toByte() -> SwingMode.FIXED_5
                    0x00.toByte() -> null // TODO: how to represent better?
                    0xFF.toByte() -> null
                    else -> throw IllegalArgumentException("Invalid SwingMode byte: ${bytes[4]}")
                },
            )
        }
    }

    public val asHexString: String
        get() = byteArrayOf(
            hours.value.toByte(),
            minutes.value.toByte(),
            when (acStatus) {
                ACStatus.ON -> 0x30.toByte()
                ACStatus.OFF -> 0x31.toByte()
            },
            when (acMode) {
                ACMode.AUTO -> 0x41.toByte()
                ACMode.COOL -> 0x42.toByte()
                ACMode.HEAT -> 0x43.toByte()
                ACMode.DRY -> 0x44.toByte()
                ACMode.FAN -> 0x45.toByte()
                null -> 0xff.toByte()
            },
            temperature?.value.asByte,
            when (fanMode) {
                FanMode.AUTO -> 0x41.toByte()
                FanMode.QUIET -> 0x31.toByte()
                FanMode.LOW -> 0x32.toByte()
                FanMode.MEDIUM_LOW -> 0x33.toByte()
                FanMode.MEDIUM -> 0x34.toByte()
                FanMode.MEDIUM_HIGH -> 0x35.toByte()
                FanMode.HIGH -> 0x36.toByte()
                null -> 0xff.toByte()
            },
            (
                when (meritBMode) {
                    MeritBMode.FIREPLACE_1 -> 0x30
                    MeritBMode.FIREPLACE_2 -> 0x20
                    MeritBMode.OFF -> 0x00
                    null -> 0xf0
                } or when (meritAMode) {
                    MeritAMode.HIGH_POWER -> 0x01
                    MeritAMode.CDU_SILENT_1 -> 0x02
                    MeritAMode.ECO -> 0x03
                    MeritAMode.HEATING_8C -> 0x04
                    MeritAMode.SLEEP_CARE -> 0x05
                    MeritAMode.FLOOR -> 0x06
                    MeritAMode.COMFORT -> 0x07
                    MeritAMode.CDU_SILENT_2 -> 0x0A
                    MeritAMode.OFF -> 0x00
                    null -> 0x0f
                }
                ).toByte(),
            when (swingMode) {
                SwingMode.OFF -> 0x31.toByte()
                SwingMode.VERTICAL -> 0x41.toByte()
                SwingMode.HORIZONTAL -> 0x42.toByte()
                SwingMode.BOTH -> 0x43.toByte()
                SwingMode.FIXED_1 -> 0x50.toByte()
                SwingMode.FIXED_2 -> 0x51.toByte()
                SwingMode.FIXED_3 -> 0x52.toByte()
                SwingMode.FIXED_4 -> 0x53.toByte()
                SwingMode.FIXED_5 -> 0x54.toByte()
                null -> 0xff.toByte()
            },
        ).toHexString()

    @JvmInline
    public value class Hours(public val value: Int)

    @JvmInline
    public value class Minutes(public val value: Int)
}
