package toshibaac.client.iot

public data class IncomingSMMobileMethodCall(
    public val sourceId: String,
    public val messageId: String,
    public val targetId: List<String>,
    public val payload: IncomingSMMobileMethodCallPayload,
    public val timeStamp: String,
    public val timeZone: Any?,
    public val fcuTime: Any?,
)
