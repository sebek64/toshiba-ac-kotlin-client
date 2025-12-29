package toshibaac.client.types

@OptIn(ExperimentalStdlibApi::class)
public data class FCUState(
    val acStatus: ACStatus?,
    val acMode: ACMode?,
    val temperature: Temperature?,
    val fanMode: FanMode?,
    val swingMode: SwingMode?,
    val powerMode: PowerMode?,
    val meritBMode: MeritBMode?,
    val meritAMode: MeritAMode?,
    val pureIonMode: PureIonMode?,
    val indoorTemperature: Temperature?,
    val outdoorTemperature: Temperature?,
    // unknown field
    // 3x some timer fields
    //   010100 for timer setting
    //   0a0000 for timer clear
    val selfCleaningMode: SelfCleaningMode?,
    // unknown field
    // schedule status 00 - disabled, 01 - enabled
    // off-timer hours
    // off-timer minutes
) {
    public companion object {
        public fun from(str: String): FCUState {
            val bytes = str.hexToByteArray()
            return FCUState(
                acStatus = when (bytes[0]) {
                    0x30.toByte() -> ACStatus.ON
                    0x31.toByte() -> ACStatus.OFF
                    0x02.toByte() -> null // TODO: how to represent better?
                    0xFF.toByte() -> null
                    else -> throw IllegalArgumentException("Invalid ACStatus byte: ${bytes[0]}")
                },
                acMode = when (bytes[1]) {
                    0x41.toByte() -> ACMode.AUTO
                    0x42.toByte() -> ACMode.COOL
                    0x43.toByte() -> ACMode.HEAT
                    0x44.toByte() -> ACMode.DRY
                    0x45.toByte() -> ACMode.FAN
                    0xFF.toByte() -> null
                    0x00.toByte() -> null // TODO: how to represent better?
                    else -> throw IllegalArgumentException("Invalid ACMode byte: ${bytes[1]}")
                },
                temperature = Temperature.fromRaw(bytes[2]),
                fanMode = when (bytes[3]) {
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
                swingMode = when (bytes[4]) {
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
                powerMode = when (bytes[5]) {
                    50.toByte() -> PowerMode.POWER_50
                    75.toByte() -> PowerMode.POWER_75
                    100.toByte() -> PowerMode.POWER_100
                    0xFF.toByte() -> null
                    else -> throw IllegalArgumentException("Invalid PowerMode byte: ${bytes[5]}")
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
                pureIonMode = when (bytes[7]) {
                    0x10.toByte() -> PureIonMode.OFF
                    0x18.toByte() -> PureIonMode.ON
                    0xFF.toByte() -> null
                    else -> throw IllegalArgumentException("Invalid PureIonMode byte: ${bytes[7]}")
                },
                indoorTemperature = Temperature.fromRaw(bytes[8]),
                outdoorTemperature = Temperature.fromRaw(bytes[9]),
                selfCleaningMode = when (bytes[14]) {
                    0x10.toByte() -> SelfCleaningMode.OFF
                    0x18.toByte() -> SelfCleaningMode.ON
                    0xFF.toByte() -> null
                    else -> throw IllegalArgumentException("Invalid SelfCleaningMode byte: ${bytes[14]}")
                },
            )
        }
    }

    public val asHexString: String
        get() = byteArrayOf(
            when (acStatus) {
                ACStatus.ON -> 0x30.toByte()
                ACStatus.OFF -> 0x31.toByte()
                null -> 0xff.toByte()
            },
            when (acMode) {
                ACMode.AUTO -> 0x41.toByte()
                ACMode.COOL -> 0x42.toByte()
                ACMode.HEAT -> 0x43.toByte()
                ACMode.DRY -> 0x44.toByte()
                ACMode.FAN -> 0x45.toByte()
                null -> 0xff.toByte()
            },
            temperature.asByte,
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
            when (powerMode) {
                PowerMode.POWER_50 -> 50.toByte()
                PowerMode.POWER_75 -> 75.toByte()
                PowerMode.POWER_100 -> 100.toByte()
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
            when (pureIonMode) {
                PureIonMode.OFF -> 0x10.toByte()
                PureIonMode.ON -> 0x18.toByte()
                null -> 0xff.toByte()
            },
            indoorTemperature.asByte,
            outdoorTemperature.asByte,
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            when (selfCleaningMode) {
                SelfCleaningMode.OFF -> 0x10.toByte()
                SelfCleaningMode.ON -> 0x18.toByte()
                null -> 0xff.toByte()
            },
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
            0xff.toByte(),
        ).toHexString()
}

public enum class ACStatus {
    ON,
    OFF,
}

public enum class ACMode {
    AUTO,
    COOL,
    HEAT,
    DRY,
    FAN,
}

public enum class FanMode {
    AUTO,
    QUIET,
    LOW,
    MEDIUM_LOW,
    MEDIUM,
    MEDIUM_HIGH,
    HIGH,
}

public enum class SwingMode {
    OFF,
    VERTICAL,
    HORIZONTAL,
    BOTH,
    FIXED_1,
    FIXED_2,
    FIXED_3,
    FIXED_4,
    FIXED_5,
}

public enum class PowerMode {
    POWER_50,
    POWER_75,
    POWER_100,
}

public enum class MeritBMode {
    FIREPLACE_1,
    FIREPLACE_2,
    OFF,
}

public enum class MeritAMode {
    HIGH_POWER,
    CDU_SILENT_1,
    ECO,
    HEATING_8C,
    SLEEP_CARE,
    FLOOR,
    COMFORT,
    CDU_SILENT_2,
    OFF,
}

public enum class PureIonMode {
    OFF,
    ON,
}

public enum class SelfCleaningMode {
    OFF,
    ON,
}
