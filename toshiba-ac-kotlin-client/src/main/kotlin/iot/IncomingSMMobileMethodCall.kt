package toshibaac.client.iot

import toshibaac.client.DeviceId
import toshibaac.client.DeviceUniqueId

public data class IncomingSMMobileMethodCall(
    public val sourceId: DeviceUniqueId,
    public val messageId: MessageId,
    public val targetId: List<DeviceId>,
    public val payload: IncomingSMMobileMethodCallPayload,
    public val timeStamp: MessageTimestamp,
)
