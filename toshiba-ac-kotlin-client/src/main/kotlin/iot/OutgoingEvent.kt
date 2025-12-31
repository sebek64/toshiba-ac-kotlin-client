package toshibaac.client.iot

import toshibaac.client.DeviceUniqueId
import toshibaac.client.types.FCUState

public sealed interface OutgoingEvent {
    public val targetId: List<DeviceUniqueId>
    public val messageId: MessageId

    public data class SetFCUParameters(
        override val targetId: List<DeviceUniqueId>,
        override val messageId: MessageId,
        val fcuState: FCUState,
    ) : OutgoingEvent
}
