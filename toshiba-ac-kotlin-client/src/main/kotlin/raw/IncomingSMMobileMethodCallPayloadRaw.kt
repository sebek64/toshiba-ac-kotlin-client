package toshibaac.client.raw

internal sealed interface IncomingSMMobileMethodCallPayloadRaw {
    data class Heartbeat(
        val iTemp: String?,
        val oTemp: String?,
        val fcuTemp: String?,
        val fcuTcjTemp: String?,
        val fcuFanRpm: String?,
        val cduTdTemp: String?,
        val cduTsTemp: String?,
        val cduTeTemp: String?,
        val cduCompHz: String?,
        val cduFanRpm: String?,
        val cduPmvPulse: String?,
        val cduIac: String?,
    ) : IncomingSMMobileMethodCallPayloadRaw

    data class FCUFromAC(
        val data: String,
    ) : IncomingSMMobileMethodCallPayloadRaw

    data class SetScheduleFromAC(
        val programSetting: ProgramSetting,
        val schedulerStatus: String,
        val dstStatus: String,
        val dst: DST,
    ) : IncomingSMMobileMethodCallPayloadRaw {
        data class ProgramSetting(
            val Sunday: Program,
            val Monday: Program,
            val Tuesday: Program,
            val Wednesday: Program,
            val Thursday: Program,
            val Friday: Program,
            val Saturday: Program,
        ) {
            data class Program(
                val p1: String,
                val p2: String,
                val p3: String,
                val p4: String,
            )
        }

        data class DST(
            val Time: String,
            val Status: String,
        )
    }
}
