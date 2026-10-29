package toshibaac.client.http

import java.time.Instant

internal data class Session(
    val tokenType: TokenType,
    val accessToken: AccessToken,
    val consumerId: ConsumerId,
    val expiresAt: Instant,
)
