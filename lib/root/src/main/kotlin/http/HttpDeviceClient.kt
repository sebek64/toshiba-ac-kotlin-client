package toshibaac.client.http

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import toshibaac.api.http.ApiResponse
import toshibaac.api.http.GetACMappingResponsePayload
import toshibaac.api.http.GetProgramSettingsResponsePayload
import toshibaac.api.http.LoginRequest
import toshibaac.api.http.LoginResponsePayload
import toshibaac.api.http.RegisterRequest
import toshibaac.api.http.RegisterResponsePayload
import toshibaac.client.DeviceId
import toshibaac.client.DeviceUniqueId
import toshibaac.client.IoTHostName
import toshibaac.client.IoTSasToken
import toshibaac.client.types.FCUState
import toshibaac.client.types.ProgramEntry
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val log = KotlinLogging.logger {}

internal class HttpDeviceClient internal constructor(
    private val httpClient: HttpClient,
) : AutoCloseable by httpClient {
    companion object {
        fun create(): HttpDeviceClient = HttpDeviceClient(
            httpClient = HttpClient.newBuilder()
                .build(),
        )

        private const val BASE_URL = "https://mobileapi.toshibahomeaccontrols.com/api/"
        private const val CONSUMER_URL = "${BASE_URL}Consumer/"
        private const val AC_URL = "${BASE_URL}AC/"
    }

    suspend fun login(
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
            consumerMasterId = ConsumerMasterId(response.consumerMasterId),
            isHeatQuantityActivated = response.isHeatQuantityActivated,
        )
    }

    suspend fun registerMobileDevice(
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

    suspend fun getACList(
        tokenType: TokenType,
        accessToken: AccessToken,
        consumerId: ConsumerId,
    ): GetACListResult {
        val response: List<GetACMappingResponsePayload> = makeRequest(
            request = HttpRequest.newBuilder()
                .uri(URI.create("${AC_URL}GetConsumerACMapping?consumerId=${consumerId.value}"))
                .header("Content-Type", "application/json")
                .header("Authorization", "${tokenType.value} ${accessToken.value}")
                .GET()
                .build(),
        ) { ApiResponse.deserialize(it) }
        return GetACListResult(
            groups = response.map { group ->
                GetACListResult.Group(
                    groupId = GroupId(group.GroupId),
                    groupName = GroupName(group.GroupName),
                    consumerId = ConsumerId(group.ConsumerId),
                    timeZone = GroupTimeZone(group.TimeZone),
                    acs = group.ACList.map { ac ->
                        GetACListResult.Group.AC(
                            id = ACId(ac.Id),
                            deviceUniqueId = DeviceUniqueId(ac.DeviceUniqueId),
                            name = ACName(ac.Name),
                            modelId = ACModelId(ac.ACModelId),
                            description = ACDescription(ac.Description),
                            fcuState = FCUState.from(ac.ACStateData),
                            meritFeature = MeritFeature(ac.MeritFeature),
                            adapterType = ACAdapterType(ac.AdapterType),
                        )
                    },
                )
            },
        )
    }

    suspend fun getProgramSettings(
        tokenType: TokenType,
        accessToken: AccessToken,
        consumerId: ConsumerId,
    ): GetProgramSettingsResult {
        val response: GetProgramSettingsResponsePayload = makeRequest(
            request = HttpRequest.newBuilder()
                .uri(URI.create("${AC_URL}GetConsumerProgramSettings?consumerId=${consumerId.value}"))
                .header("Content-Type", "application/json")
                .header("Authorization", "${tokenType.value} ${accessToken.value}")
                .GET()
                .build(),
        ) { ApiResponse.deserialize(it) }
        return GetProgramSettingsResult(
            groupSettings = response.ACGroupProgramSettings.map { groupSetting ->
                GetProgramSettingsResult.GroupSetting(
                    groupId = GroupId(groupSetting.GroupId),
                    groupName = GroupName(groupSetting.GroupName),
                    acSettings = groupSetting.ACProgramSettingList.map { acSetting ->
                        GetProgramSettingsResult.GroupSetting.ACSetting(
                            id = ACId(acSetting.ACId),
                            deviceUniqueId = DeviceUniqueId(acSetting.ACUniqueId),
                            name = ACName(acSetting.ACName),
                            model = ACModelId(acSetting.ACModel),
                            timeZone = acSetting.timeZone,
                            dstStatus = acSetting.dstStatus,
                            schedulerStatus = acSetting.schedulerStatus,
                            state = FCUState.from(acSetting.ACStateDataForProgram),
                            meritFeature = MeritFeature(acSetting.MeritFeature),
                            programSetting = acSetting.programSetting.toProgramSetting(),
                        )
                    },
                    programSetting = groupSetting.programSetting.toProgramSetting(),
                )
            },
        )
    }

    private fun GetProgramSettingsResponsePayload.ProgramSetting.toProgramSetting(): GetProgramSettingsResult.ProgramSetting = GetProgramSettingsResult.ProgramSetting(
        sunday = Sunday.toProgram(),
        monday = Monday.toProgram(),
        tuesday = Tuesday.toProgram(),
        wednesday = Wednesday.toProgram(),
        thursday = Thursday.toProgram(),
        friday = Friday.toProgram(),
        saturday = Saturday.toProgram(),
    )

    private fun GetProgramSettingsResponsePayload.ProgramSetting.Program.toProgram(): GetProgramSettingsResult.ProgramSetting.Program = GetProgramSettingsResult.ProgramSetting.Program(
        p1 = ProgramEntry.from(this.p1),
        p2 = ProgramEntry.from(this.p2),
        p3 = ProgramEntry.from(this.p3),
        p4 = ProgramEntry.from(this.p4),
        p5 = ProgramEntry.from(this.p5),
        p6 = ProgramEntry.from(this.p6),
        p7 = ProgramEntry.from(this.p7),
        p8 = ProgramEntry.from(this.p8),
        p9 = ProgramEntry.from(this.p9),
        p10 = ProgramEntry.from(this.p10),
    )

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
