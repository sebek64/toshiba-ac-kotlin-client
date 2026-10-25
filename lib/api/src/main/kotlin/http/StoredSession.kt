package toshibaac.api.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class StoredSession(
    val username: String,
    val tokenType: String,
    val accessToken: String,
    val consumerId: String,
    val expiresAtEpochSeconds: Long,
) {
    public companion object {
        public fun deserializeOrNull(str: String): StoredSession? = try {
            Json.decodeFromString<StoredSession>(str)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    public fun serialize(): String = Json.encodeToString(this)
}
