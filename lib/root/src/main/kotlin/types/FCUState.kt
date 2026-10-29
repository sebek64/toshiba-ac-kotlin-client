package toshibaac.client.types

import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

public data class FCUState(
    val acStatus: ACStatus?,
    val acMode: ACMode?,
    /** Without the 8°C offset; when sending it in 8°C heating, [meritAMode] must be set as well. */
    val temperature: TargetTemperature?,
    val fanMode: FanMode?,
    val swingMode: SwingMode?,
    val powerMode: PowerMode?,
    val meritBMode: MeritBMode?,
    val meritAMode: MeritAMode?,
    val pureIonMode: PureIonMode?,
    val indoorTemperature: IndoorTemperature?,
    val outdoorTemperature: OutdoorTemperature?,
    /** Reported by the AC only, never sent. */
    val errorCode: ACErrorCode?,
    /** When sending [Timer.None], set [acStatus] as well, the clear command differs for an AC that is off. */
    val timer: Timer?,
    /** Reported by the AC only, never sent. */
    val selfCleaningMode: SelfCleaningMode?,
    val defrost: Defrost?,
    /** LED on the Wi-Fi adapter. */
    val wifiLedStatus: WifiLedStatus?,
    val schedulerStatus: SchedulerStatus?,
) {
    internal companion object {
        fun from(str: String): FCUState {
            val bytes = str.hexToByteArray()

            fun scheduledTimer(create: (Duration, LocalTime) -> Timer): Timer? {
                val delayHours = bytes[12].toInt() and 0xFF
                val delayMinutes = bytes[13].toInt() and 0xFF
                val setAtHour = bytes[17].toInt() and 0xFF
                val setAtMinute = bytes[18].toInt() and 0xFF
                return when {
                    delayHours == 0xFF || delayMinutes == 0xFF || setAtHour > 23 || setAtMinute > 59 -> null
                    else -> create(delayHours.hours + delayMinutes.minutes, LocalTime.of(setAtHour, setAtMinute))
                }
            }

            return FCUState(
                acStatus = when (bytes[0]) {
                    0x30.toByte() -> ACStatus.ON
                    0x31.toByte() -> ACStatus.OFF
                    else -> null
                },
                acMode = when (bytes[1]) {
                    0x41.toByte() -> ACMode.AUTO
                    0x42.toByte() -> ACMode.COOL
                    0x43.toByte() -> ACMode.HEAT
                    0x44.toByte() -> ACMode.DRY
                    0x45.toByte() -> ACMode.FAN
                    else -> null
                },
                temperature = Temperature.fromRaw(bytes[2])?.let { TargetTemperature(it.fromHeating8CRaw(bytes[6])) },
                fanMode = when (bytes[3]) {
                    0x41.toByte() -> FanMode.AUTO
                    0x31.toByte() -> FanMode.QUIET
                    0x32.toByte() -> FanMode.LOW
                    0x33.toByte() -> FanMode.MEDIUM_LOW
                    0x34.toByte() -> FanMode.MEDIUM
                    0x35.toByte() -> FanMode.MEDIUM_HIGH
                    0x36.toByte() -> FanMode.HIGH
                    else -> null
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
                    0x60.toByte() -> SwingMode.HADA
                    0x61.toByte() -> SwingMode.FIXED_HORIZONTAL_1
                    0x62.toByte() -> SwingMode.FIXED_HORIZONTAL_2
                    0x63.toByte() -> SwingMode.FIXED_HORIZONTAL_3
                    0x64.toByte() -> SwingMode.FIXED_HORIZONTAL_4
                    0x65.toByte() -> SwingMode.FIXED_HORIZONTAL_5
                    else -> null
                },
                powerMode = when (bytes[5]) {
                    50.toByte() -> PowerMode.POWER_50
                    75.toByte() -> PowerMode.POWER_75
                    100.toByte() -> PowerMode.POWER_100
                    else -> null
                },
                meritBMode = when ((bytes[6].toUInt() shr 4).toByte()) {
                    0x02.toByte() -> MeritBMode.FIREPLACE_1
                    0x03.toByte() -> MeritBMode.FIREPLACE_2
                    0x01.toByte() -> MeritBMode.OFF
                    0x00.toByte() -> MeritBMode.OFF
                    else -> null
                },
                meritAMode = when (bytes[6].toUInt() and 0x0Fu) {
                    0x01u -> MeritAMode.HIGH_POWER
                    0x02u -> MeritAMode.CDU_SILENT_1
                    0x03u -> MeritAMode.ECO
                    0x04u -> MeritAMode.HEATING_8C
                    0x05u -> MeritAMode.SLEEP_CARE
                    0x06u -> MeritAMode.FLOOR
                    0x07u -> MeritAMode.COMFORT
                    0x0Au -> MeritAMode.CDU_SILENT_2
                    0x00u -> MeritAMode.OFF
                    else -> null
                },
                pureIonMode = when (bytes[7]) {
                    0x10.toByte() -> PureIonMode.OFF
                    0x18.toByte() -> PureIonMode.ON
                    else -> null
                },
                indoorTemperature = Temperature.fromRaw(bytes[8])?.let { IndoorTemperature(it) },
                outdoorTemperature = Temperature.fromRaw(bytes[9])?.let { OutdoorTemperature(it) },
                errorCode = when (val code = bytes[10].toInt() and 0xFF) {
                    0xFF, 0xFE, 0x00 -> null
                    else -> acErrorCodeOf(code)
                },
                timer = when (bytes[11].toInt() and 0xFF) {
                    0x01 -> scheduledTimer(Timer.Scheduled::Off)
                    0x03 -> scheduledTimer(Timer.Scheduled::On)
                    0x05 -> scheduledTimer(Timer.Scheduled::OnOff)
                    0x02, 0x04, 0x06, 0x09, 0x0A, 0x0B -> Timer.None
                    else -> null
                },
                selfCleaningMode = when {
                    bytes[14] == 0xFF.toByte() -> null
                    (bytes[14].toInt() and 0x18) == 0x18 -> SelfCleaningMode.ON
                    else -> SelfCleaningMode.OFF
                },
                defrost = when (bytes[14]) {
                    0xFF.toByte() -> null
                    else -> Defrost(
                        strong = (bytes[14].toInt() and 0x01) != 0,
                        onDemand = (bytes[14].toInt() and 0x02) != 0,
                    )
                },
                wifiLedStatus = when (bytes[15]) {
                    0x01.toByte() -> WifiLedStatus.ON
                    0x02.toByte() -> WifiLedStatus.OFF
                    else -> null
                },
                schedulerStatus = when (bytes[16]) {
                    0x01.toByte() -> SchedulerStatus.ENABLED
                    0x02.toByte() -> SchedulerStatus.DISABLED
                    else -> null
                },
            )
        }
    }

    internal val asHexString: String
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
            temperature?.value?.toHeating8CRaw(meritAMode).asByte,
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
                SwingMode.HADA -> 0x60.toByte()
                SwingMode.FIXED_HORIZONTAL_1 -> 0x61.toByte()
                SwingMode.FIXED_HORIZONTAL_2 -> 0x62.toByte()
                SwingMode.FIXED_HORIZONTAL_3 -> 0x63.toByte()
                SwingMode.FIXED_HORIZONTAL_4 -> 0x64.toByte()
                SwingMode.FIXED_HORIZONTAL_5 -> 0x65.toByte()
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
                    MeritBMode.FIREPLACE_1 -> 0x20
                    MeritBMode.FIREPLACE_2 -> 0x30
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
            indoorTemperature?.value.asByte,
            outdoorTemperature?.value.asByte,
            0xff.toByte(),
            when (timer) {
                null, is Timer.Scheduled.OnOff -> 0xff.toByte()
                Timer.None -> if (acStatus == ACStatus.OFF) 0x09.toByte() else 0x0a.toByte()
                is Timer.Scheduled.Off -> 0x01.toByte()
                is Timer.Scheduled.On -> 0x03.toByte()
            },
            when (timer) {
                null, is Timer.Scheduled.OnOff -> 0xff.toByte()
                Timer.None -> 0x00.toByte()
                is Timer.Scheduled -> timer.delay.inWholeHours.toByte()
            },
            when (timer) {
                null, is Timer.Scheduled.OnOff -> 0xff.toByte()
                Timer.None -> 0x00.toByte()
                is Timer.Scheduled -> (timer.delay.inWholeMinutes % 60).toByte()
            },
            when (defrost) {
                null -> 0xff.toByte()
                else -> ((if (defrost.strong) 0x01 else 0x00) or (if (defrost.onDemand) 0x02 else 0x00)).toByte()
            },
            when (wifiLedStatus) {
                WifiLedStatus.ON -> 0x01.toByte()
                WifiLedStatus.OFF -> 0x02.toByte()
                null -> 0xff.toByte()
            },
            when (schedulerStatus) {
                SchedulerStatus.ENABLED -> 0x01.toByte()
                SchedulerStatus.DISABLED -> 0x02.toByte()
                null -> 0xff.toByte()
            },
            when (timer) {
                null, is Timer.Scheduled.OnOff -> 0xff.toByte()
                Timer.None -> 0x00.toByte()
                is Timer.Scheduled -> timer.setAt.hour.toByte()
            },
            when (timer) {
                null, is Timer.Scheduled.OnOff -> 0xff.toByte()
                Timer.None -> 0x00.toByte()
                is Timer.Scheduled -> timer.setAt.minute.toByte()
            },
        ).toHexString()
}
