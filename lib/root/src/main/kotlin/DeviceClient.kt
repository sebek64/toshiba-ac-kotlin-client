package toshibaac.client

import io.github.oshai.kotlinlogging.KotlinLogging
import toshibaac.api.http.StoredSession
import toshibaac.client.http.AccessToken
import toshibaac.client.http.ConsumerId
import toshibaac.client.http.DeviceType
import toshibaac.client.http.GetACListResult
import toshibaac.client.http.GetProgramSettingsResult
import toshibaac.client.http.HttpDeviceClient
import toshibaac.client.http.Password
import toshibaac.client.http.Session
import toshibaac.client.http.TokenType
import toshibaac.client.http.Username
import toshibaac.client.iot.IncomingEvent
import toshibaac.client.iot.IoTDeviceClient
import java.time.Duration
import java.time.Instant

private val log = KotlinLogging.logger {}

private val EXPIRY_MARGIN = Duration.ofMinutes(5)

public class DeviceClient private constructor(
    private val httpDeviceClient: HttpDeviceClient,
    private val username: Username,
    private val session: Session,
) : AutoCloseable by httpDeviceClient {
    public companion object {
        /**
         * Reuses a valid session from [sessionStore] if there is one, otherwise logs in and stores the new session.
         */
        public suspend fun create(
            username: Username,
            password: Password,
            sessionStore: SessionStore? = null,
        ): DeviceClient {
            val httpDeviceClient = HttpDeviceClient.create()
            return try {
                val session = sessionStore?.loadValidSession(username)
                    ?: httpDeviceClient.loginAndStore(
                        username = username,
                        password = password,
                        sessionStore = sessionStore,
                    )
                DeviceClient(
                    httpDeviceClient = httpDeviceClient,
                    username = username,
                    session = session,
                )
            } catch (e: Throwable) {
                httpDeviceClient.close()
                throw e
            }
        }
    }

    public suspend fun getACList(): GetACListResult = httpDeviceClient.getACList(
        tokenType = session.tokenType,
        accessToken = session.accessToken,
        consumerId = session.consumerId,
    )

    public suspend fun getProgramSettings(): GetProgramSettingsResult = httpDeviceClient.getProgramSettings(
        tokenType = session.tokenType,
        accessToken = session.accessToken,
        consumerId = session.consumerId,
    )

    public suspend fun registerAndLaunchIoTClient(
        deviceId: DeviceId,
        onIncomingEvent: (IncomingEvent) -> Unit,
        onConnectionLost: (Throwable) -> Unit,
    ): IoTDeviceClient {
        val registerResult = httpDeviceClient.registerMobileDevice(
            tokenType = session.tokenType,
            accessToken = session.accessToken,
            deviceId = deviceId,
            deviceType = DeviceType("1"),
            userName = username,
        )
        return IoTDeviceClient.create(
            hostName = registerResult.hostName,
            deviceId = registerResult.deviceId,
            sasToken = registerResult.sasToken,
            onIncomingEvent = onIncomingEvent,
            onConnectionLost = onConnectionLost,
        )
    }
}

private fun Session.isExpired(): Boolean = Instant.now() >= expiresAt - EXPIRY_MARGIN

private suspend fun SessionStore.loadValidSession(username: Username): Session? {
    val stored = load()?.let { StoredSession.deserializeOrNull(it) }
    if (stored == null || stored.username != username.value) {
        return null
    }
    val session = Session(
        tokenType = TokenType(stored.tokenType),
        accessToken = AccessToken(stored.accessToken),
        consumerId = ConsumerId(stored.consumerId),
        expiresAt = Instant.ofEpochSecond(stored.expiresAtEpochSeconds),
    )
    if (session.isExpired()) {
        log.info { "Stored session expired at ${session.expiresAt}" }
        return null
    }
    log.info { "Reusing stored session valid until ${session.expiresAt}" }
    return session
}

private suspend fun HttpDeviceClient.loginAndStore(
    username: Username,
    password: Password,
    sessionStore: SessionStore?,
): Session {
    log.info { "Logging in as ${username.value}" }
    val loginResult = login(
        username = username,
        password = password,
    )
    val session = Session(
        tokenType = loginResult.tokenType,
        accessToken = loginResult.accessToken,
        consumerId = loginResult.consumerId,
        expiresAt = Instant.now().plusSeconds(loginResult.expiresIn.value),
    )
    sessionStore?.save(
        StoredSession(
            username = username.value,
            tokenType = session.tokenType.value,
            accessToken = session.accessToken.value,
            consumerId = session.consumerId.value,
            expiresAtEpochSeconds = session.expiresAt.epochSecond,
        ).serialize(),
    )
    return session
}
