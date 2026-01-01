package toshibaac.client.iot

import toshibaac.client.DeviceId
import toshibaac.client.DeviceUniqueId
import toshibaac.client.types.FCUState
import toshibaac.client.types.Temperature

public sealed interface IncomingEvent {
    public val sourceId: DeviceUniqueId
    public val messageId: MessageId
    public val targetId: List<DeviceId>
    public val timeStamp: MessageTimestamp

    public data class Heartbeat(
        override val sourceId: DeviceUniqueId,
        override val messageId: MessageId,
        override val targetId: List<DeviceId>,
        override val timeStamp: MessageTimestamp,
        public val iTemp: Temperature?,
        public val oTemp: Temperature?,
        public val fcuTcTemp: Temperature?,
        public val fcuTcjTemp: Temperature?,
        public val fcuFanRpm: String?,
        public val cduTdTemp: Temperature?,
        public val cduTsTemp: Temperature?,
        public val cduTeTemp: Temperature?,
        public val cduCompHz: String?,
        public val cduFanRpm: String?,
        public val cduPmvPulse: String?,
        public val cduIac: String?,
    ) : IncomingEvent

    public data class FCUFromAC(
        override val sourceId: DeviceUniqueId,
        override val messageId: MessageId,
        override val targetId: List<DeviceId>,
        override val timeStamp: MessageTimestamp,
        public val data: FCUState,
    ) : IncomingEvent

    // TODO: correct parsed content, now just raw copy
    public data class SetScheduleFromAC(
        override val sourceId: DeviceUniqueId,
        override val messageId: MessageId,
        override val targetId: List<DeviceId>,
        override val timeStamp: MessageTimestamp,
        public val programSetting: ProgramSetting,
        public val schedulerStatus: String,
        public val dstStatus: String,
        public val dst: DST,
    ) : IncomingEvent {
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
