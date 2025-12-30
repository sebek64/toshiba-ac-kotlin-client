package toshibaac.client.iot

import toshibaac.client.DeviceUniqueId
import toshibaac.client.types.FCUState

public sealed interface OutgoingEvent {
    public val targetId: List<DeviceUniqueId>

    public data class SetFCUParameters(
        override val targetId: List<DeviceUniqueId>,
        val fcuState: FCUState,
    ) : OutgoingEvent
}
