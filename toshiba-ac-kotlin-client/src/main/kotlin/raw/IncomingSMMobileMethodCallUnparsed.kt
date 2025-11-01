package toshibaac.client.raw

public data class IncomingSMMobileMethodCallUnparsed(
    public val sourceId: String,
    public val messageId: String,
    public val targetId: List<String>,
    public val payload: IncomingSMMobileMethodCallPayloadRaw,
    public val timeStamp: String,
    public val timeZone: Any?,
    public val fcuTime: Any?,
)
