package toshibaac.client.iot

import kotlinx.serialization.json.Json
import toshibaac.client.FCUState
import toshibaac.client.IncomingSMMobileMethodCall
import toshibaac.client.IncomingSMMobileMethodCallPayload
import toshibaac.client.OutgoingMessage
import toshibaac.client.Temperature

public class HighLevelClient private constructor(
    private val client: CloseableIoTDeviceClient,
) : AutoCloseable by client {
    public companion object {
        public suspend fun create(
            connectionInfo: ConnectionInfo,
            messageCallback: (IncomingSMMobileMethodCall) -> Unit,
        ): HighLevelClient = HighLevelClient(
            CloseableIoTDeviceClient.create(
                connectionInfo = connectionInfo,
            ) { unparsed ->
                messageCallback(
                    IncomingSMMobileMethodCall(
                        sourceId = unparsed.sourceId,
                        messageId = unparsed.messageId,
                        targetId = unparsed.targetId,
                        timeStamp = unparsed.timeStamp,
                        timeZone = unparsed.timeZone,
                        fcuTime = unparsed.fcuTime,
                        payload = when (val unparsedPayload = unparsed.payload) {
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
                        },
                    ),
                )
            },
        )
    }

    public suspend fun sendMsg(
        rawMsg: OutgoingMessage,
    ) {
        val msgStr = Json.encodeToString<OutgoingMessage>(rawMsg)
        client.sendMsg(msgStr)
    }
}
