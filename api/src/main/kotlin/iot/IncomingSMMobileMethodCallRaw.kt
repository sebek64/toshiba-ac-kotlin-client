package toshibaac.api.iot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("cmd")
public sealed interface IncomingSMMobileMethodCallRaw {
    public companion object {
        public fun deserialize(str: String): IncomingSMMobileMethodCallRaw = Json.decodeFromString(str)
    }

    public val sourceId: String
    public val messageId: String
    public val targetId: List<String>
    public val timeStamp: String
    public val payload: Any
    public val timeZone: JsonElement
    public val fcuTime: JsonElement

    @Serializable
    @SerialName("CMD_HEARTBEAT")
    public data class Heartbeat(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        override val timeStamp: String,
        override val payload: Payload,
        override val timeZone: JsonElement,
        override val fcuTime: JsonElement,
    ) : IncomingSMMobileMethodCallRaw {
        @Serializable
        public data class Payload(
            val iTemp: String?,
            val oTemp: String?,
            val fcuTcTemp: String?,
            val fcuTcjTemp: String?,
            val fcuFanRpm: String?,
            val cduTdTemp: String?,
            val cduTsTemp: String?,
            val cduTeTemp: String?,
            val cduCompHz: String?,
            val cduFanRpm: String?,
            val cduPmvPulse: String?,
            val cduIac: String?,
        )
    }

    @Serializable
    @SerialName("CMD_FCU_FROM_AC")
    public data class FCUFromAC(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        override val timeStamp: String,
        override val payload: Payload,
        override val timeZone: JsonElement,
        override val fcuTime: JsonElement,
    ) : IncomingSMMobileMethodCallRaw {
        @Serializable
        public data class Payload(
            val data: String,
        )
    }

    @Serializable
    @SerialName("CMD_SET_SCHEDULE_FROM_AC")
    public data class SetScheduleFromAC(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        override val timeStamp: String,
        override val payload: Payload,
        override val timeZone: JsonElement,
        override val fcuTime: JsonElement,
    ) : IncomingSMMobileMethodCallRaw {
        @Serializable
        public data class Payload(
            val programSetting: ProgramSetting,
            val schedulerStatus: String,
            val dstStatus: String,
            val dst: DST,
        ) {
            @Serializable
            public data class ProgramSetting(
                val Sunday: Program,
                val Monday: Program,
                val Tuesday: Program,
                val Wednesday: Program,
                val Thursday: Program,
                val Friday: Program,
                val Saturday: Program,
            ) {
                @Serializable
                public data class Program(
                    val p1: String,
                    val p2: String,
                    val p3: String,
                    val p4: String,
                )
            }

            @Serializable
            public data class DST(
                val Time: String,
                val Status: String,
            )
        }
    }
}
