package toshibaac.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withTimeout
import toshibaac.client.DeviceClient
import toshibaac.client.http.ACName
import toshibaac.client.http.GetACListResult
import toshibaac.client.http.GetProgramSettingsResult
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
import kotlin.time.Duration

private val log = KotlinLogging.logger {}

internal sealed interface Command {
    suspend fun execute(
        deviceClient: SuspendLazy<DeviceClient>,
        iotClientWrapper: LazyCloseable<IoTClientWrapper>,
    )

    data class Status(
        private val printDefaults: Boolean,
        private val listenForUpdatesFor: Duration,
    ) : Command {
        override suspend fun execute(
            deviceClient: SuspendLazy<DeviceClient>,
            iotClientWrapper: LazyCloseable<IoTClientWrapper>,
        ) {
            if (listenForUpdatesFor.isPositive()) {
                withTimeout(listenForUpdatesFor) {
                    iotClientWrapper.get().incomingEvents
                        .onSubscription {
                            deviceClient.get().getACList().prettyPrint()
                        }
                        .collect { incomingEvent ->
                            // TODO: pretty-print and resolve IDs to names
                            println("Received update message: $incomingEvent")
                        }
                }
            } else {
                deviceClient.get().getACList().prettyPrint()
            }
        }

        private fun GetACListResult.prettyPrint() {
            groups.forEach { group ->
                println("Group: ${group.groupName.value}")
                group.acs.forEach { ac ->
                    println("  AC: ${ac.name.value} ${ac.fcuState.acStatus}")
                    ac.fcuState.prettyPrint(printDefaults)
                }
            }
        }
    }

    class QuerySchedule : Command {
        override suspend fun execute(
            deviceClient: SuspendLazy<DeviceClient>,
            iotClientWrapper: LazyCloseable<IoTClientWrapper>,
        ) {
            deviceClient.get().getProgramSettings().groupSettings.forEach { groupSetting ->
                println("Group: ${groupSetting.groupName.value}")
                groupSetting.programSetting.prettyPrint("  ")
                groupSetting.acSettings.forEach { acSetting ->
                    println("  AC: ${acSetting.name.value}")
                    acSetting.state.prettyPrint(printDefaults = false)
                    acSetting.programSetting.prettyPrint("    ")
                }
            }
        }

        private fun GetProgramSettingsResult.ProgramSetting.prettyPrint(prefix: String) {
            sunday.prettyPrint(prefix = prefix, dayName = "Sunday")
            monday.prettyPrint(prefix = prefix, dayName = "Monday")
            tuesday.prettyPrint(prefix = prefix, dayName = "Tuesday")
            wednesday.prettyPrint(prefix = prefix, dayName = "Wednesday")
            thursday.prettyPrint(prefix = prefix, dayName = "Thursday")
            friday.prettyPrint(prefix = prefix, dayName = "Friday")
            saturday.prettyPrint(prefix = prefix, dayName = "Saturday")
        }

        private fun GetProgramSettingsResult.ProgramSetting.Program.prettyPrint(
            prefix: String,
            dayName: String,
        ) {
            println("$prefix$dayName:")
            listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10).forEach { programEntry ->
                if (programEntry != null) {
                    print("  $prefix${programEntry.hours.value}:${programEntry.minutes.value} -> ${programEntry.acStatus}")
                    programEntry.acMode?.let { value ->
                        print(" Mode: $value")
                    }
                    programEntry.temperature?.let { value ->
                        print(" Target Temperature: ${value.value.value}°C")
                    }
                    programEntry.fanMode?.let { value ->
                        print(" Fan mode: $value")
                    }
                    programEntry.meritAMode?.let { value ->
                        print(" Merit A Mode: $value")
                    }
                    programEntry.meritBMode?.let { value ->
                        print(" Merit B Mode: $value")
                    }
                    programEntry.swingMode?.let { value ->
                        print(" Swing mode: $value")
                    }
                    println()
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

private fun FCUState.prettyPrint(printDefaults: Boolean) {
    acStatus?.let { value ->
        println("    Status: $value")
    }
    acMode?.let { value ->
        println("    Mode: $value")
    }
    fanMode?.let { value ->
        if (printDefaults || value != FanMode.AUTO) {
            println("    Fan mode: $value")
        }
    }
    swingMode?.let { value ->
        if (printDefaults || value != SwingMode.OFF) {
            println("    Swing mode: $value")
        }
    }
    powerMode?.let { value ->
        if (printDefaults || value != PowerMode.POWER_100) {
            println("    Power mode: $value")
        }
    }
    temperature?.let { value ->
        println("    Target Temperature: ${value.value.value}°C")
    }
    indoorTemperature?.let { value ->
        println("    Indoor Temperature: ${value.value.value}°C")
    }
    outdoorTemperature?.let { value ->
        println("    Outdoor Temperature: ${value.value.value}°C")
    }
    meritAMode?.let { value ->
        if (printDefaults || value != MeritAMode.OFF) {
            println("    Merit A Mode: $value")
        }
    }
    meritBMode?.let { value ->
        if (printDefaults || value != MeritBMode.OFF) {
            println("    Merit B Mode: $value")
        }
    }
    pureIonMode?.let { value ->
        if (printDefaults || value != PureIonMode.OFF) {
            println("    Pure Ion Mode: $value")
        }
    }
    selfCleaningMode?.let { value ->
        if (printDefaults || value != SelfCleaningMode.OFF) {
            println("    Self Cleaning Mode: $value")
        }
    }
}
