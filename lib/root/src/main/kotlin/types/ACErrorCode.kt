package toshibaac.client.types

public sealed interface ACErrorCode {
    public val code: Int

    public data object Communication : ACErrorCode {
        override val code: Int = 0x01
    }

    public data object OutdoorUnit : ACErrorCode {
        override val code: Int = 0x02
    }

    /** Other error (including compressor). */
    public data object Other : ACErrorCode {
        override val code: Int = 0x03
    }

    public data object SerialSignal : ACErrorCode {
        override val code: Int = 0x04
    }

    public data object CaseThermostat : ACErrorCode {
        override val code: Int = 0x07
    }

    /** TA sensor (room temperature) error. */
    public data object TaSensor : ACErrorCode {
        override val code: Int = 0x0C
    }

    /** TC sensor (heat exchanger temperature) error. */
    public data object TcSensor : ACErrorCode {
        override val code: Int = 0x0D
    }

    /** TCJ sensor (heat exchanger temperature) error. */
    public data object TcjSensor : ACErrorCode {
        override val code: Int = 0x0F
    }

    public data object IndoorFanMotor : ACErrorCode {
        override val code: Int = 0x11
    }

    public data object IndoorPcb : ACErrorCode {
        override val code: Int = 0x12
    }

    public data object InverterOverCurrent : ACErrorCode {
        override val code: Int = 0x14
    }

    public data object InverterPositionDetection : ACErrorCode {
        override val code: Int = 0x16
    }

    public data object CurrentSensor : ACErrorCode {
        override val code: Int = 0x17
    }

    /** TE sensor (outdoor heat exchanger temperature) or TS sensor (suction pipe temperature) error. */
    public data object TeOrTsSensor : ACErrorCode {
        override val code: Int = 0x18
    }

    /** TD sensor (discharge pipe temperature) error. */
    public data object TdSensor : ACErrorCode {
        override val code: Int = 0x19
    }

    public data object OutdoorFanMotor : ACErrorCode {
        override val code: Int = 0x1A
    }

    /** TO sensor (outdoor temperature) error. */
    public data object ToSensor : ACErrorCode {
        override val code: Int = 0x1B
    }

    /** Compressor system or other error. */
    public data object CompressorSystem : ACErrorCode {
        override val code: Int = 0x1C
    }

    /** Compressor does not rotate. */
    public data object CompressorLock : ACErrorCode {
        override val code: Int = 0x1D
    }

    /** Discharge temperature over limit. */
    public data object DischargeTemperature : ACErrorCode {
        override val code: Int = 0x1E
    }

    public data object CompressorBreakdown : ACErrorCode {
        override val code: Int = 0x1F
    }

    public data object HighPressure : ACErrorCode {
        override val code: Int = 0x21
    }

    public data object FaultStatus : ACErrorCode {
        override val code: Int = 0x34
    }

    public data class Unrecognized(override val code: Int) : ACErrorCode
}

private val knownErrorCodes = listOf(
    ACErrorCode.Communication,
    ACErrorCode.OutdoorUnit,
    ACErrorCode.Other,
    ACErrorCode.SerialSignal,
    ACErrorCode.CaseThermostat,
    ACErrorCode.TaSensor,
    ACErrorCode.TcSensor,
    ACErrorCode.TcjSensor,
    ACErrorCode.IndoorFanMotor,
    ACErrorCode.IndoorPcb,
    ACErrorCode.InverterOverCurrent,
    ACErrorCode.InverterPositionDetection,
    ACErrorCode.CurrentSensor,
    ACErrorCode.TeOrTsSensor,
    ACErrorCode.TdSensor,
    ACErrorCode.OutdoorFanMotor,
    ACErrorCode.ToSensor,
    ACErrorCode.CompressorSystem,
    ACErrorCode.CompressorLock,
    ACErrorCode.DischargeTemperature,
    ACErrorCode.CompressorBreakdown,
    ACErrorCode.HighPressure,
    ACErrorCode.FaultStatus,
)

internal fun acErrorCodeOf(code: Int): ACErrorCode = knownErrorCodes.firstOrNull { it.code == code }
    ?: ACErrorCode.Unrecognized(code)
