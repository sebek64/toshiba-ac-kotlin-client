package toshibaac.client.iot

import kotlin.uuid.Uuid

@JvmInline
public value class MessageId(public val value: String) {
    public companion object {
        public fun random(): MessageId = MessageId(Uuid.random().toString().dropLast(8))
    }
}
