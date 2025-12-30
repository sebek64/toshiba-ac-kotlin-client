package toshibaac.api.http

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("IsSuccess")
public sealed interface ApiResponse<out P> {
    public companion object {
        public inline fun <reified P> deserialize(str: String): ApiResponse<P> = Json.decodeFromString(str)
    }

    @Serializable
    @SerialName("true")
    public data class Success<out P>(
        val ResObj: P,
        val Message: String,
        val StatusCode: String,
    ) : ApiResponse<P>

    @Serializable
    @SerialName("false")
    public data class Failure(
        val ResObj: JsonElement,
        val Message: String,
        val StatusCode: String,
    ) : ApiResponse<Nothing>
}
