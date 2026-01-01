package toshibaac.cli

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import toshibaac.client.DeviceClient
import toshibaac.client.DeviceId
import toshibaac.client.iot.IncomingEvent
import toshibaac.client.iot.IoTDeviceClient

internal class IoTClientWrapper private constructor(
    val iotClient: IoTDeviceClient,
    val incomingEvents: SharedFlow<IncomingEvent>,
) : AutoCloseable by iotClient {
    companion object {
        suspend fun create(
            deviceClient: DeviceClient,
            deviceId: DeviceId,
        ): IoTClientWrapper {
            val incomingEvents = MutableSharedFlow<IncomingEvent>(replay = 0, extraBufferCapacity = 10)
            return IoTClientWrapper(
                iotClient = deviceClient.registerAndLaunchIoTClient(
                    deviceId = deviceId,
                ) { incomingEvent ->
                    incomingEvents.tryEmit(incomingEvent)
                },
                incomingEvents = incomingEvents,
            )
        }
    }
}
