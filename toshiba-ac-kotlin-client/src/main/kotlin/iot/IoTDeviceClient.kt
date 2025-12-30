package toshibaac.client.iot

import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.Message
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import toshibaac.api.iot.IncomingSMMobileMethodCallRaw
import toshibaac.api.iot.OutgoingMessage
import toshibaac.client.DeviceId
import toshibaac.client.IoTHostName
import toshibaac.client.IoTSasToken
import toshibaac.client.types.FCUState
import toshibaac.client.types.Temperature

private val log = KotlinLogging.logger {}

public class IoTDeviceClient private constructor(
    private val client: DeviceClient,
) : AutoCloseable {
    public companion object {
        public suspend fun create(
            hostName: IoTHostName,
            deviceId: DeviceId,
            sasToken: IoTSasToken,
            messageCallback: (IncomingSMMobileMethodCall) -> Unit,
        ): IoTDeviceClient {
            val client = DeviceClient(
                hostName.value,
                deviceId.value,
                {
                    sasToken.value.toCharArray()
                },
                IotHubClientProtocol.AMQPS,
            )
            client.open(false)
            val ready = CompletableDeferred<IoTDeviceClient>()
            client.subscribeToMethodsAsync(
                { name, payload, _ ->
                    try {
                        if (name != "smmobile") {
                            log.warn { "Unknown method call $name with payload ${payload.payloadAsJsonString}" }
                            return@subscribeToMethodsAsync DirectMethodResponse(404, null)
                        }
                        val payloadStr = payload.payloadAsJsonString
                        log.info { "Received method call with payload $payloadStr" }
                        val parsedPayload = IncomingSMMobileMethodCallRaw.deserialize(payloadStr)
                        messageCallback(
                            IncomingSMMobileMethodCall(
                                sourceId = parsedPayload.sourceId,
                                messageId = parsedPayload.messageId,
                                targetId = parsedPayload.targetId,
                                payload = parsedPayload.parse(),
                                timeStamp = parsedPayload.timeStamp,
                            ),
                        )
                        DirectMethodResponse(200, null)
                    } catch (e: Exception) {
                        log.error(e) { "Error processing method call $name" }
                        DirectMethodResponse(500, null)
                    }
                },
                null,
                { exception, _ ->
                    if (exception != null) {
                        ready.completeExceptionally(exception)
                    }
                    val deviceClient = IoTDeviceClient(
                        client = client,
                    )
                    ready.complete(deviceClient)
                },
                null,
            )
            return ready.await()
        }
    }

    private suspend fun sendMsg(
        message: OutgoingMessage,
    ) {
        val msgStr = message.serialize()
        val deferred = CompletableDeferred<Unit>()
        client.sendEventAsync(
            Message(msgStr).also { message ->
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

private fun IncomingSMMobileMethodCallRaw.parse() = when (val unparsedCall = this) {
    is IncomingSMMobileMethodCallRaw.FCUFromAC -> IncomingSMMobileMethodCallPayload.FCUFromAC(
        data = FCUState.from(unparsedCall.payload.data),
    )

    is IncomingSMMobileMethodCallRaw.Heartbeat -> IncomingSMMobileMethodCallPayload.Heartbeat(
        iTemp = Temperature.fromRaw(unparsedCall.payload.iTemp),
        oTemp = Temperature.fromRaw(unparsedCall.payload.oTemp),
        fcuTcTemp = Temperature.fromRaw(unparsedCall.payload.fcuTcTemp),
        fcuTcjTemp = Temperature.fromRaw(unparsedCall.payload.fcuTcjTemp),
        fcuFanRpm = unparsedCall.payload.fcuFanRpm,
        cduTdTemp = Temperature.fromRaw(unparsedCall.payload.cduTdTemp),
        cduTsTemp = Temperature.fromRaw(unparsedCall.payload.cduTsTemp),
        cduTeTemp = Temperature.fromRaw(unparsedCall.payload.cduTeTemp),
        cduCompHz = unparsedCall.payload.cduCompHz,
        cduFanRpm = unparsedCall.payload.cduFanRpm,
        cduPmvPulse = unparsedCall.payload.cduPmvPulse,
        cduIac = unparsedCall.payload.cduIac,
    )

    is IncomingSMMobileMethodCallRaw.SetScheduleFromAC -> IncomingSMMobileMethodCallPayload.SetScheduleFromAC(
        programSetting = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting(
            sunday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Sunday.p1,
                p2 = unparsedCall.payload.programSetting.Sunday.p2,
                p3 = unparsedCall.payload.programSetting.Sunday.p3,
                p4 = unparsedCall.payload.programSetting.Sunday.p4,
            ),
            monday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Monday.p1,
                p2 = unparsedCall.payload.programSetting.Monday.p2,
                p3 = unparsedCall.payload.programSetting.Monday.p3,
                p4 = unparsedCall.payload.programSetting.Monday.p4,
            ),
            tuesday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Tuesday.p1,
                p2 = unparsedCall.payload.programSetting.Tuesday.p2,
                p3 = unparsedCall.payload.programSetting.Tuesday.p3,
                p4 = unparsedCall.payload.programSetting.Tuesday.p4,
            ),
            wednesday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Wednesday.p1,
                p2 = unparsedCall.payload.programSetting.Wednesday.p2,
                p3 = unparsedCall.payload.programSetting.Wednesday.p3,
                p4 = unparsedCall.payload.programSetting.Wednesday.p4,
            ),
            thursday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Thursday.p1,
                p2 = unparsedCall.payload.programSetting.Thursday.p2,
                p3 = unparsedCall.payload.programSetting.Thursday.p3,
                p4 = unparsedCall.payload.programSetting.Thursday.p4,
            ),
            friday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Friday.p1,
                p2 = unparsedCall.payload.programSetting.Friday.p2,
                p3 = unparsedCall.payload.programSetting.Friday.p3,
                p4 = unparsedCall.payload.programSetting.Friday.p4,
            ),
            saturday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedCall.payload.programSetting.Saturday.p1,
                p2 = unparsedCall.payload.programSetting.Saturday.p2,
                p3 = unparsedCall.payload.programSetting.Saturday.p3,
                p4 = unparsedCall.payload.programSetting.Saturday.p4,
            ),
        ),
        schedulerStatus = unparsedCall.payload.schedulerStatus,
        dstStatus = unparsedCall.payload.dstStatus,
        dst = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.DST(
            time = unparsedCall.payload.dst.Time,
            status = unparsedCall.payload.dst.Status,
        ),
    )
}
