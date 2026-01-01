package toshibaac.api.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class LoginRequest(
    val Username: String,
    val Password: String,
) {
    public fun serialize(): String = Json.encodeToString(this)
}
