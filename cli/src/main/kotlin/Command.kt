package toshibaac.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.take
import toshibaac.client.DeviceClient
import toshibaac.client.http.ACName
import toshibaac.client.http.GetACListResult
import toshibaac.client.iot.IncomingEvent
import toshibaac.client.iot.MessageId
import toshibaac.client.iot.OutgoingEvent
import toshibaac.client.types.FCUState
import toshibaac.client.types.FanMode
import toshibaac.client.types.MeritAMode
import toshibaac.client.types.MeritBMode
import toshibaac.client.types.PowerMode
import toshibaac.client.types.PureIonMode
import toshibaac.client.types.SelfCleaningMode
import toshibaac.client.types.SwingMode

private val log = KotlinLogging.logger {}

internal sealed interface Command {
    suspend fun execute(
        deviceClient: SuspendLazy<DeviceClient>,
        iotClientWrapper: LazyCloseable<IoTClientWrapper>,
    )

    data class Status(
        private val printDefaults: Boolean,
    ) : Command {
        override suspend fun execute(
            deviceClient: SuspendLazy<DeviceClient>,
            iotClientWrapper: LazyCloseable<IoTClientWrapper>,
        ) {
            deviceClient.get().getACList().print()
        }

        private fun GetACListResult.print() {
            groups.forEach { group ->
                println("Group: ${group.groupName.value}")
                group.acs.forEach { ac ->
                    println("  AC: ${ac.name.value} ${ac.fcuState.acStatus}")
                    ac.fcuState.acStatus?.let { value ->
                        println("    Status: $value")
                    }
                    ac.fcuState.acMode?.let { value ->
                        println("    Mode: $value")
                    }
                    ac.fcuState.fanMode?.let { value ->
                        if (printDefaults || value != FanMode.AUTO) {
                            println("    Fan mode: $value")
                        }
                    }
                    ac.fcuState.swingMode?.let { value ->
                        if (printDefaults || value != SwingMode.OFF) {
                            println("    Swing mode: $value")
                        }
                    }
                    ac.fcuState.powerMode?.let { value ->
                        if (printDefaults || value != PowerMode.POWER_100) {
                            println("    Power mode: $value")
                        }
                    }
                    ac.fcuState.temperature?.let { value ->
                        println("    Target Temperature: ${value.value}°C")
                    }
                    ac.fcuState.indoorTemperature?.let { value ->
                        println("    Indoor Temperature: ${value.value}°C")
                    }
                    ac.fcuState.outdoorTemperature?.let { value ->
                        println("    Outdoor Temperature: ${value.value}°C")
                    }
                    ac.fcuState.meritAMode?.let { value ->
                        if (printDefaults || value != MeritAMode.OFF) {
                            println("    Merit A Mode: $value")
                        }
                    }
                    ac.fcuState.meritBMode?.let { value ->
                        if (printDefaults || value != MeritBMode.OFF) {
                            println("    Merit B Mode: $value")
                        }
                    }
                    ac.fcuState.pureIonMode?.let { value ->
                        if (printDefaults || value != PureIonMode.OFF) {
                            println("    Pure Ion Mode: $value")
                        }
                    }
                    ac.fcuState.selfCleaningMode?.let { value ->
                        if (printDefaults || value != SelfCleaningMode.OFF) {
                            println("    Self Cleaning Mode: $value")
                        }
                    }
                }
            }
        }
    }

    data class SetParameters(
        private val acNames: List<ACName>,
        private val fcuState: FCUState,
    ) : Command {
        override suspend fun execute(
            deviceClient: SuspendLazy<DeviceClient>,
            iotClientWrapper: LazyCloseable<IoTClientWrapper>,
        ) {
            val acList = deviceClient.get().getACList()
            val acs = acList.groups.flatMap { it.acs }
            val uidToName = acNames.associateBy { name ->
                val ac = acs.firstOrNull { it.name == name } ?: error("AC $name not found")
                ac.deviceUniqueId
            }
            val iotClient = iotClientWrapper.get()
            val messageId = MessageId.random()
            iotClient.incomingEvents
                .onSubscription {
                    iotClient.iotClient.sendEvent(
                        OutgoingEvent.SetFCUParameters(
                            targetId = uidToName.keys.toList(),
                            messageId = messageId,
                            fcuState = fcuState,
                        ),
                    )
                }
                .filter { incomingEvent ->
                    when (incomingEvent) {
                        is IncomingEvent.Heartbeat -> false
                        is IncomingEvent.SetScheduleFromAC -> false
                        is IncomingEvent.FCUFromAC -> incomingEvent.messageId == messageId
                    }
                }
                .take(acNames.size)
                .collect { incomingEvent ->
                    val sourceId = incomingEvent.sourceId
                    when (val targetName = uidToName[sourceId]) {
                        null -> log.warn { "Received confirmation from unknown device ${sourceId.value}" }
                        else -> log.info { "Received confirmation for ${targetName.value}" }
                    }
                }
        }
    }
}
