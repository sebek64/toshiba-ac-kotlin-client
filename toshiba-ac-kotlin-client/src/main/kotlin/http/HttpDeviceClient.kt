package toshibaac.client.http

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import toshibaac.api.http.ApiResponse
import toshibaac.api.http.GetACMappingResponsePayload
import toshibaac.api.http.LoginRequest
import toshibaac.api.http.LoginResponsePayload
import toshibaac.api.http.RegisterRequest
import toshibaac.api.http.RegisterResponsePayload
import toshibaac.client.DeviceId
import toshibaac.client.IoTHostName
import toshibaac.client.IoTSasToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val log = KotlinLogging.logger {}

public class HttpDeviceClient internal constructor(
    private val httpClient: HttpClient,
) : AutoCloseable by httpClient {
    public companion object {
        public fun create(): HttpDeviceClient = HttpDeviceClient(
            httpClient = HttpClient.newBuilder()
                .build(),
        )

        private const val BASE_URL = "https://mobileapi.toshibahomeaccontrols.com/api/"
        private const val CONSUMER_URL = "${BASE_URL}Consumer/"
        private const val AC_URL = "${BASE_URL}AC/"
    }

    public suspend fun login(
        username: Username,
        password: Password,
    ): LoginResult {
        val response: LoginResponsePayload = makeRequest(
            HttpRequest.newBuilder()
                .uri(URI.create("${CONSUMER_URL}Login"))
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        LoginRequest(
                            Username = username.value,
                            Password = password.value,
                        ).serialize(),
                    ),
                )
                .build(),
        ) { ApiResponse.deserialize(it) }
        return LoginResult(
            accessToken = AccessToken(response.access_token),
            tokenType = TokenType(response.token_type),
            expiresIn = ExpiresIn(response.expires_in),
            consumerId = ConsumerId(response.consumerId),
            countryId = CountryId(response.countryId),
            consumerMasterId = response.consumerMasterId,
            isHeatQuantityActivated = when (response.isHeatQuantityActivated) {
                "False" -> false
                "True" -> true
                else -> error("Unknown value for isHeatQuantityActivated: ${response.isHeatQuantityActivated}")
            },
        )
    }

    public suspend fun registerMobileDevice(
        tokenType: TokenType,
        accessToken: AccessToken,
        deviceId: DeviceId,
        deviceType: DeviceType,
        userName: Username,
    ): RegisterResult {
        val response: RegisterResponsePayload = makeRequest(
            HttpRequest.newBuilder()
                .uri(URI.create("${CONSUMER_URL}RegisterMobileDevice"))
                .header("Content-Type", "application/json")
                .header("Authorization", "${tokenType.value} ${accessToken.value}")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        RegisterRequest(
                            DeviceID = deviceId.value,
                            DeviceType = deviceType.value,
                            Username = userName.value,
                        ).serialize(),
                    ),
                )
                .build(),
        ) { ApiResponse.deserialize(it) }
        return RegisterResult(
            deviceId = DeviceId(response.DeviceId),
            hostName = IoTHostName(response.HostName),
            primaryKey = PrimaryKey(response.PrimaryKey),
            secondaryKey = SecondaryKey(response.SecondaryKey),
            sasToken = IoTSasToken(response.SasToken),
            registerDate = RegisterDate(response.RegisterDate),
        )
    }

    public suspend fun getACList(
        tokenType: TokenType,
        accessToken: AccessToken,
        consumerId: ConsumerId,
    ): List<GetACMappingResponsePayload> = makeRequest(
        request = HttpRequest.newBuilder()
            .uri(URI.create("${AC_URL}GetConsumerACMapping?consumerId=${consumerId.value}"))
            .header("Content-Type", "application/json")
            .header("Authorization", "${tokenType.value} ${accessToken.value}")
            .GET()
            .build(),
    ) { ApiResponse.deserialize(it) }

    private suspend fun <P> makeRequest(
        request: HttpRequest,
        deserializeResponse: (String) -> ApiResponse<P>,
    ): P {
        val response = withContext(Dispatchers.IO) {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        }

        check(response.statusCode() == 200) {
            "Login failed with status code ${response.statusCode()}: ${response.body()}"
        }

        val bodyStr = response.body()
        val body = try {
            deserializeResponse(bodyStr)
        } catch (e: Exception) {
            log.error(e) { "Failed to deserialize response body: $bodyStr" }
            throw e
        }

        return when (body) {
            is ApiResponse.Failure -> error("HTTP request failed - ${body.StatusCode}: ${body.Message}")
            is ApiResponse.Success<P> -> body.ResObj
        }
    }
}
