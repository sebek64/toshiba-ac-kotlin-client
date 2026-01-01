package toshibaac.api.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class RegisterRequest(
    val DeviceID: String,
    val DeviceType: String,
    val Username: String,
) {
    public fun serialize(): String = Json.encodeToString(this)
}
