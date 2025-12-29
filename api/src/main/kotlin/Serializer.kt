package toshibaac.api

import kotlinx.serialization.json.Json

internal val jsonSerializer: Json = Json {
    ignoreUnknownKeys = true
}
