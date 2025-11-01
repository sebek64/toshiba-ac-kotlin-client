package toshibaac.client.raw

import com.google.gson.JsonElement

internal data class IncomingSMMobileMethodCallRaw(
    val sourceId: String,
    val messageId: String,
    val targetId: List<String>,
    val cmd: String,
    val payload: JsonElement,
    val timeStamp: String,
    val timeZone: Any?,
    val fcuTime: Any?,
)
