package toshibaac.client

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("cmd")
public sealed interface OutgoingMessage {
    public val sourceId: String
    public val messageId: String
    public val targetId: List<String>
    public val timeStamp: String

    @Serializable
    @SerialName("CMD_FCU_TO_AC")
    public class FCUToAC(
        override val sourceId: String,
        override val messageId: String,
        override val targetId: List<String>,
        public val payload: Payload,
        override val timeStamp: String,
    ) : OutgoingMessage {
        @Serializable
        public data class Payload(
            val data: String,
        )
    }
}
