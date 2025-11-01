package toshibaac.client

import com.google.gson.GsonBuilder
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.Message
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import toshibaac.client.raw.IncomingSMMobileMethodCallPayloadRaw
import toshibaac.client.raw.IncomingSMMobileMethodCallRaw
import toshibaac.client.raw.IncomingSMMobileMethodCallUnparsed

private val log = KotlinLogging.logger {}

public class CloseableIoTDeviceClient private constructor(
    private val client: DeviceClient,
) : AutoCloseable {
    public companion object {
        public suspend fun create(
            connectionInfo: ConnectionInfo,
            messageCallback: (IncomingSMMobileMethodCallUnparsed) -> Unit,
        ): CloseableIoTDeviceClient {
            val client = DeviceClient(
                connectionInfo.hostName,
                connectionInfo.deviceId,
                {
                    connectionInfo.sasToken.toCharArray()
                },
                IotHubClientProtocol.AMQPS,
            )
            client.open(false)
            val ready = CompletableDeferred<CloseableIoTDeviceClient>()
            client.subscribeToMethodsAsync(
                { name, payload, _ ->
                    if (name != "smmobile") {
                        log.warn { "Unknown method call $name with payload ${payload.payloadAsJsonElement}" }
                        return@subscribeToMethodsAsync DirectMethodResponse(404, null)
                    }
                    val parsedToplevel = payload.getPayload(IncomingSMMobileMethodCallRaw::class.java)
                    val parsedPayload = when (parsedToplevel.cmd) {
                        "CMD_SET_SCHEDULE_FROM_AC" -> {
                            GsonBuilder().create()
                                .fromJson(parsedToplevel.payload, IncomingSMMobileMethodCallPayloadRaw.SetScheduleFromAC::class.java)
                        }

                        "CMD_FCU_FROM_AC" -> {
                            GsonBuilder().create().fromJson(parsedToplevel.payload, IncomingSMMobileMethodCallPayloadRaw.FCUFromAC::class.java)
                        }

                        "CMD_HEARTBEAT" -> {
                            GsonBuilder().create().fromJson(parsedToplevel.payload, IncomingSMMobileMethodCallPayloadRaw.Heartbeat::class.java)
                        }

                        else -> {
                            log.warn { "Unknown method call payload ${parsedToplevel.cmd}" }
                            return@subscribeToMethodsAsync DirectMethodResponse(400, null)
                        }
                    }
                    messageCallback(
                        IncomingSMMobileMethodCallUnparsed(
                            sourceId = parsedToplevel.sourceId,
                            messageId = parsedToplevel.messageId,
                            targetId = parsedToplevel.targetId,
                            payload = parsedPayload,
                            timeStamp = parsedToplevel.timeStamp,
                            timeZone = parsedToplevel.timeZone,
                            fcuTime = parsedToplevel.fcuTime,
                        ),
                    )
                    DirectMethodResponse(200, null)
                },
                null,
                { exception, _ ->
                    if (exception != null) {
                        ready.completeExceptionally(exception)
                    }
                    val deviceClient = CloseableIoTDeviceClient(
                        client = client,
                    )
                    ready.complete(deviceClient)
                },
                null,
            )
            return ready.await()
        }
    }

    public suspend fun sendMsg(
        message: String,
    ) {
        val deferred = CompletableDeferred<Unit>()
        client.sendEventAsync(
            Message(message).also { message ->
                message.contentType = "application/json"
                message.contentEncoding = "utf-8"
                message.setProperty("type", "mob")
            },
            { _, exception, _ ->
                if (exception != null) {
                    log.warn(exception) { "Error sending message $message" }
                    deferred.completeExceptionally(exception)
                }
                deferred.complete(Unit)
            },
            null,
        )
        deferred.await()
    }

    override fun close() {
        client.close()
    }
}
