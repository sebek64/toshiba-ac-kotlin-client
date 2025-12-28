package toshibaac.client.raw

internal data class IncomingSMMobileMethodCallUnparsed(
    val sourceId: String,
    val messageId: String,
    val targetId: List<String>,
    val payload: IncomingSMMobileMethodCallPayloadRaw,
    val timeStamp: String,
    val timeZone: Any?,
    val fcuTime: Any?,
)
