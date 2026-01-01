package toshibaac.client.iot

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
public value class MessageId(public val value: String) {
    public companion object {
        @OptIn(ExperimentalUuidApi::class)
        public fun random(): MessageId = MessageId(Uuid.random().toString().dropLast(8))
    }
}
