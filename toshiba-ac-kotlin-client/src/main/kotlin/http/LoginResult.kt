package toshibaac.client.http

internal data class LoginResult(
    val accessToken: AccessToken,
    val tokenType: TokenType,
    val expiresIn: ExpiresIn,
    val consumerId: ConsumerId,
    val countryId: CountryId,
    val consumerMasterId: ConsumerMasterId,
    val isHeatQuantityActivated: Boolean,
)
