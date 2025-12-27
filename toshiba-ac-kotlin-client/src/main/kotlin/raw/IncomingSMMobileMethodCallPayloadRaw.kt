package toshibaac.client.raw

public sealed interface IncomingSMMobileMethodCallPayloadRaw {
    public data class Heartbeat(
        public val iTemp: String?,
        public val oTemp: String?,
        public val fcuTemp: String?,
        public val fcuTcjTemp: String?,
        public val fcuFanRpm: String?,
        public val cduTdTemp: String?,
        public val cduTsTemp: String?,
        public val cduTeTemp: String?,
        public val cduCompHz: String?,
        public val cduFanRpm: String?,
        public val cduPmvPulse: String?,
        public val cduIac: String?,
    ) : IncomingSMMobileMethodCallPayloadRaw

    public data class FCUFromAC(
        public val data: String,
    ) : IncomingSMMobileMethodCallPayloadRaw

    public data class SetScheduleFromAC(
        public val programSetting: ProgramSetting,
        public val schedulerStatus: String,
        public val dstStatus: String,
        public val dst: DST,
    ) : IncomingSMMobileMethodCallPayloadRaw {
        public data class ProgramSetting(
            public val Sunday: Program,
            public val Monday: Program,
            public val Tuesday: Program,
            public val Wednesday: Program,
            public val Thursday: Program,
            public val Friday: Program,
            public val Saturday: Program,
        ) {
            public data class Program(
                public val p1: String,
                public val p2: String,
                public val p3: String,
                public val p4: String,
            )
        }

        public data class DST(
            public val Time: String,
            public val Status: String,
        )
    }
}
