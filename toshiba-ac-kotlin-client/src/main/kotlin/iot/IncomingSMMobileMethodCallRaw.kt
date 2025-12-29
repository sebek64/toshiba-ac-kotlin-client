package toshibaac.client.iot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("cmd")
internal sealed interface IncomingSMMobileMethodCallRaw {
    val sourceId: String
    val messageId: String
    val targetId: List<String>
    val timeStamp: String
    val payload: Any
    // TODO: receiving nulls only, not sure about correct type
    // val timeZone: Any?,
    // val fcuTime: Any?,

    @Serializable
    @SerialName("CMD_HEARTBEAT")
    data class Heartbeat(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        override val timeStamp: String,
        override val payload: Payload,
    ) : IncomingSMMobileMethodCallRaw {
        @Serializable
        data class Payload(
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
    data class FCUFromAC(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        override val timeStamp: String,
        override val payload: Payload,
    ) : IncomingSMMobileMethodCallRaw {
        @Serializable
        data class Payload(
            val data: String,
        )
    }

    @Serializable
    @SerialName("CMD_SET_SCHEDULE_FROM_AC")
    data class SetScheduleFromAC(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        override val timeStamp: String,
        override val payload: Payload,
    ) : IncomingSMMobileMethodCallRaw {
        @Serializable
        data class Payload(
            val programSetting: ProgramSetting,
            val schedulerStatus: String,
            val dstStatus: String,
            val dst: DST,
        ) {
            @Serializable
            data class ProgramSetting(
                val Sunday: Program,
                val Monday: Program,
                val Tuesday: Program,
                val Wednesday: Program,
                val Thursday: Program,
                val Friday: Program,
                val Saturday: Program,
            ) {
                @Serializable
                data class Program(
                    val p1: String,
                    val p2: String,
                    val p3: String,
                    val p4: String,
                )
            }

            @Serializable
            data class DST(
                val Time: String,
                val Status: String,
            )
        }
    }
}
