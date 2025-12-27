package toshibaac.client

public sealed interface IncomingSMMobileMethodCallPayload {
    public data class Heartbeat(
        public val iTemp: Temperature?,
        public val oTemp: Temperature?,
        public val fcuTemp: Temperature?,
        public val fcuTcjTemp: Temperature?,
        public val fcuFanRpm: String?,
        public val cduTdTemp: Temperature?,
        public val cduTsTemp: Temperature?,
        public val cduTeTemp: Temperature?,
        public val cduCompHz: String?,
        public val cduFanRpm: String?,
        public val cduPmvPulse: String?,
        public val cduIac: String?,
    ) : IncomingSMMobileMethodCallPayload

    public data class FCUFromAC(
        public val data: FCUState,
    ) : IncomingSMMobileMethodCallPayload

    // TODO: correct parsed content, now just raw copy
    public data class SetScheduleFromAC(
        public val programSetting: ProgramSetting,
        public val schedulerStatus: String,
        public val dstStatus: String,
        public val dst: DST,
    ) : IncomingSMMobileMethodCallPayload {
        public data class ProgramSetting(
            public val sunday: Program,
            public val monday: Program,
            public val tuesday: Program,
            public val wednesday: Program,
            public val thursday: Program,
            public val friday: Program,
            public val saturday: Program,
        ) {
            public data class Program(
                public val p1: String,
                public val p2: String,
                public val p3: String,
                public val p4: String,
            )
        }

        public data class DST(
            public val time: String,
            public val status: String,
        )
    }
}
