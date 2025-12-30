package toshibaac.client

import toshibaac.client.http.DeviceType
import toshibaac.client.http.GetACListResult
import toshibaac.client.http.HttpDeviceClient
import toshibaac.client.http.LoginResult
import toshibaac.client.http.Password
import toshibaac.client.http.Username
import toshibaac.client.iot.IncomingEvent
import toshibaac.client.iot.IoTDeviceClient

public class DeviceClient private constructor(
    private val httpDeviceClient: HttpDeviceClient,
    private val username: Username,
    private val loginResult: LoginResult,
) : AutoCloseable by httpDeviceClient {
    public companion object {
        public suspend fun create(
            username: Username,
            password: Password,
        ): DeviceClient {
            val httpDeviceClient = HttpDeviceClient.create()
            return try {
                val loginResult = httpDeviceClient.login(
                    username = username,
                    password = password,
                )
                DeviceClient(
                    httpDeviceClient = httpDeviceClient,
                    username = username,
                    loginResult = loginResult,
                )
            } catch (e: Throwable) {
                httpDeviceClient.close()
                throw e
            }
        }
    }

    public suspend fun getACList(): GetACListResult = httpDeviceClient.getACList(
        tokenType = loginResult.tokenType,
        accessToken = loginResult.accessToken,
        consumerId = loginResult.consumerId,
    )

    public suspend fun registerAndLaunchIoTClient(
        deviceId: DeviceId,
        onIncomingEvent: (IncomingEvent) -> Unit,
    ): IoTDeviceClient {
        val registerResult = httpDeviceClient.registerMobileDevice(
            tokenType = loginResult.tokenType,
            accessToken = loginResult.accessToken,
            deviceId = deviceId,
            deviceType = DeviceType("1"),
            userName = username,
        )
        return IoTDeviceClient.create(
            hostName = registerResult.hostName,
            deviceId = registerResult.deviceId,
            sasToken = registerResult.sasToken,
            onIncomingEvent = onIncomingEvent,
        )
    }
}
