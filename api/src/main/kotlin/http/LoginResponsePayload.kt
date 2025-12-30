package toshibaac.api.http

import kotlinx.serialization.Serializable

@Serializable
public data class LoginResponsePayload(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val consumerId: String,
    val countryId: Int,
    val consumerMasterId: String,
    val isHeatQuantityActivated: String,
)
