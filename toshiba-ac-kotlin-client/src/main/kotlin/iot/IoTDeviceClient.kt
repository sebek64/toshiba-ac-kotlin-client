package toshibaac.client.iot

import com.google.gson.GsonBuilder
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.Message
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import toshibaac.client.types.FCUState
import toshibaac.client.types.Temperature

private val log = KotlinLogging.logger {}

public class IoTDeviceClient private constructor(
    private val client: DeviceClient,
) : AutoCloseable {
    public companion object {
        public suspend fun create(
            connectionInfo: ConnectionInfo,
            messageCallback: (IncomingSMMobileMethodCall) -> Unit,
        ): IoTDeviceClient {
            val client = DeviceClient(
                connectionInfo.hostName,
                connectionInfo.deviceId,
                {
                    connectionInfo.sasToken.toCharArray()
                },
                IotHubClientProtocol.AMQPS,
            )
            client.open(false)
            val ready = CompletableDeferred<IoTDeviceClient>()
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
                        IncomingSMMobileMethodCall(
                            sourceId = parsedToplevel.sourceId,
                            messageId = parsedToplevel.messageId,
                            targetId = parsedToplevel.targetId,
                            payload = parsedPayload.parse(),
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
        val msgStr = Json.encodeToString<OutgoingMessage>(message)
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

private fun IncomingSMMobileMethodCallPayloadRaw.parse() = when (val unparsedPayload = this) {
    is IncomingSMMobileMethodCallPayloadRaw.FCUFromAC -> IncomingSMMobileMethodCallPayload.FCUFromAC(
        data = FCUState.from(unparsedPayload.data),
    )

    is IncomingSMMobileMethodCallPayloadRaw.Heartbeat -> IncomingSMMobileMethodCallPayload.Heartbeat(
        iTemp = Temperature.fromRaw(unparsedPayload.iTemp),
        oTemp = Temperature.fromRaw(unparsedPayload.oTemp),
        fcuTemp = Temperature.fromRaw(unparsedPayload.fcuTemp),
        fcuTcjTemp = Temperature.fromRaw(unparsedPayload.fcuTcjTemp),
        fcuFanRpm = unparsedPayload.fcuFanRpm,
        cduTdTemp = Temperature.fromRaw(unparsedPayload.cduTdTemp),
        cduTsTemp = Temperature.fromRaw(unparsedPayload.cduTsTemp),
        cduTeTemp = Temperature.fromRaw(unparsedPayload.cduTeTemp),
        cduCompHz = unparsedPayload.cduCompHz,
        cduFanRpm = unparsedPayload.cduFanRpm,
        cduPmvPulse = unparsedPayload.cduPmvPulse,
        cduIac = unparsedPayload.cduIac,
    )

    is IncomingSMMobileMethodCallPayloadRaw.SetScheduleFromAC -> IncomingSMMobileMethodCallPayload.SetScheduleFromAC(
        programSetting = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting(
            sunday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Sunday.p1,
                p2 = unparsedPayload.programSetting.Sunday.p2,
                p3 = unparsedPayload.programSetting.Sunday.p3,
                p4 = unparsedPayload.programSetting.Sunday.p4,
            ),
            monday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Monday.p1,
                p2 = unparsedPayload.programSetting.Monday.p2,
                p3 = unparsedPayload.programSetting.Monday.p3,
                p4 = unparsedPayload.programSetting.Monday.p4,
            ),
            tuesday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Tuesday.p1,
                p2 = unparsedPayload.programSetting.Tuesday.p2,
                p3 = unparsedPayload.programSetting.Tuesday.p3,
                p4 = unparsedPayload.programSetting.Tuesday.p4,
            ),
            wednesday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Wednesday.p1,
                p2 = unparsedPayload.programSetting.Wednesday.p2,
                p3 = unparsedPayload.programSetting.Wednesday.p3,
                p4 = unparsedPayload.programSetting.Wednesday.p4,
            ),
            thursday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Thursday.p1,
                p2 = unparsedPayload.programSetting.Thursday.p2,
                p3 = unparsedPayload.programSetting.Thursday.p3,
                p4 = unparsedPayload.programSetting.Thursday.p4,
            ),
            friday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Friday.p1,
                p2 = unparsedPayload.programSetting.Friday.p2,
                p3 = unparsedPayload.programSetting.Friday.p3,
                p4 = unparsedPayload.programSetting.Friday.p4,
            ),
            saturday = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.ProgramSetting.Program(
                p1 = unparsedPayload.programSetting.Saturday.p1,
                p2 = unparsedPayload.programSetting.Saturday.p2,
                p3 = unparsedPayload.programSetting.Saturday.p3,
                p4 = unparsedPayload.programSetting.Saturday.p4,
            ),
        ),
        schedulerStatus = unparsedPayload.schedulerStatus,
        dstStatus = unparsedPayload.dstStatus,
        dst = IncomingSMMobileMethodCallPayload.SetScheduleFromAC.DST(
            time = unparsedPayload.dst.Time,
            status = unparsedPayload.dst.Status,
        ),
    )
}
