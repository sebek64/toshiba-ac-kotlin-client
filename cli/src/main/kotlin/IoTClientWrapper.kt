package toshibaac.cli

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import toshibaac.client.DeviceClient
import toshibaac.client.DeviceId
import toshibaac.client.iot.IncomingEvent
import toshibaac.client.iot.IoTDeviceClient

internal class IoTClientWrapper private constructor(
    val iotClient: IoTDeviceClient,
    private val events: Channel<IncomingEvent>,
) : AutoCloseable {
    companion object {
        suspend fun create(
            deviceClient: DeviceClient,
            deviceId: DeviceId,
        ): IoTClientWrapper {
            val events = Channel<IncomingEvent>(Channel.UNLIMITED)
            return IoTClientWrapper(
                iotClient = deviceClient.registerAndLaunchIoTClient(
                    deviceId = deviceId,
                ) { incomingEvent ->
                    events.trySend(incomingEvent)
                },
                events = events,
            )
        }
    }

    val incomingEvents: Flow<IncomingEvent> = events.receiveAsFlow()

    override fun close() {
        iotClient.close()
        events.close()
    }
}
