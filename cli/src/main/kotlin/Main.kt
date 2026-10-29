package toshibaac.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import toshibaac.client.DeviceClient
import toshibaac.client.DeviceId
import toshibaac.client.http.ACName
import toshibaac.client.http.Password
import toshibaac.client.http.Username
import toshibaac.client.types.ACMode
import toshibaac.client.types.ACStatus
import toshibaac.client.types.AirDirection
import toshibaac.client.types.Defrost
import toshibaac.client.types.FCUState
import toshibaac.client.types.FanMode
import toshibaac.client.types.MeritAMode
import toshibaac.client.types.MeritBMode
import toshibaac.client.types.PowerMode
import toshibaac.client.types.PureIonMode
import toshibaac.client.types.SchedulerStatus
import toshibaac.client.types.SmartSensingStatus
import toshibaac.client.types.SwingMode
import toshibaac.client.types.TargetTemperature
import toshibaac.client.types.Temperature
import toshibaac.client.types.Timer
import toshibaac.client.types.WifiLedStatus
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.concurrent.thread
import kotlin.time.Duration

public suspend fun main(args: Array<String>) {
    val mutableContextBuilder = MutableContextBuilder()
    ToshibaCLICommand(mutableContextBuilder)
        .subcommands(
            StatusCommand(mutableContextBuilder),
            QueryScheduleCommand(mutableContextBuilder),
            SetParametersCommand(mutableContextBuilder),
        )
        .main(args)

    val context = mutableContextBuilder.build()

    coroutineScope {
        val job = launch(Dispatchers.Default) {
            LazyCloseable {
                DeviceClient.create(
                    username = context.username,
                    password = context.password,
                )
            }.use { deviceClient ->
                LazyCloseable {
                    deviceClient.get().registerAndLaunchIoTClient(
                        deviceId = context.deviceId,
                    )
                }.use { iotClient ->
                    context.commands.forEach { command ->
                        command.execute(
                            deviceClient = deviceClient,
                            iotClient = iotClient,
                        )
                    }
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                runBlocking {
                    job.cancelAndJoin()
                }
            },
        )
    }
}

private class ToshibaCLICommand(
    private val mutableContextBuilder: MutableContextBuilder,
) : CliktCommand() {
    override val allowMultipleSubcommands = true

    private val username: Username by option(
        help = "Username for Toshiba AC account",
        envvar = "TAC_USERNAME",
    )
        .convert { Username(it) }
        .required()

    private val password: Password by option(
        help = "Password for Toshiba AC account",
        envvar = "TAC_PASSWORD",
    )
        .convert { Password(it) }
        .required()

    private val defaultDeviceIdSuffix = "_3e6e4eb5f0e5aa41"

    private val deviceId: DeviceId? by option(
        help = "ID of this mobile device (defaults to <username>$defaultDeviceIdSuffix)",
        envvar = "TAC_DEVICE_ID",
    )
        .convert { DeviceId(it) }

    override fun run() {
        mutableContextBuilder.setGlobals(
            username = username,
            password = password,
            deviceId = deviceId ?: DeviceId("${username.value}$defaultDeviceIdSuffix"),
        )
    }
}

private class StatusCommand(
    private val mutableContextBuilder: MutableContextBuilder,
) : CliktCommand() {
    private val printDefaults: Boolean by option(
        "--print-default-modes",
        help = "Print obvious default values too",
    )
        .flag(default = false)

    private val listenForUpdatesFor: Duration by mutuallyExclusiveOptions(
        option(
            "--listen-for",
            help = "Listen for updates for the specified duration (e.g., 30s, 5m)",
        )
            .convert { Duration.parse(it) },
        option(
            "--listen",
            help = "Listen for updates indefinitely",
        )
            .flag(default = false)
            .convert { value ->
                when (value) {
                    true -> Duration.INFINITE
                    false -> Duration.ZERO
                }
            },
    )
        .single()
        .default(Duration.ZERO)

    override fun run() {
        mutableContextBuilder += Command.Status(
            printDefaults = printDefaults,
            listenForUpdatesFor = listenForUpdatesFor,
        )
    }
}

private class QueryScheduleCommand(
    private val mutableContextBuilder: MutableContextBuilder,
) : CliktCommand() {
    override fun run() {
        mutableContextBuilder += Command.QuerySchedule()
    }
}

private class SetParametersCommand(
    private val mutableContextBuilder: MutableContextBuilder,
) : CliktCommand() {
    private val acNames: List<ACName> by option(
        "-n",
        "--name",
        help = "Name of the AC unit to control",
    )
        .convert { ACName(it) }
        .multiple(required = true)

    private val status: ACStatus? by option(help = "Target AC status")
        .choice("on" to ACStatus.ON, "off" to ACStatus.OFF)

    private val mode: ACMode? by option(help = "Target AC mode")
        .choice(
            "auto" to ACMode.AUTO,
            "cool" to ACMode.COOL,
            "heat" to ACMode.HEAT,
            "dry" to ACMode.DRY,
            "fan" to ACMode.FAN,
        )

    private val temperature: TargetTemperature? by option(help = "Target temperature in Celsius")
        .convert { TargetTemperature(Temperature(it.toInt())) }

    private val fanMode: FanMode? by option(help = "Target fan mode")
        .choice(
            "auto" to FanMode.AUTO,
            "quiet" to FanMode.QUIET,
            "low" to FanMode.LOW,
            "medium_low" to FanMode.MEDIUM_LOW,
            "medium" to FanMode.MEDIUM,
            "medium_high" to FanMode.MEDIUM_HIGH,
            "high" to FanMode.HIGH,
        )

    private val swingMode: SwingMode? by option(help = "Target swing mode")
        .choice(
            "off" to SwingMode.OFF,
            "vertical" to SwingMode.VERTICAL,
            "horizontal" to SwingMode.HORIZONTAL,
            "both" to SwingMode.BOTH,
            "fixed_1" to SwingMode.FIXED_1,
            "fixed_2" to SwingMode.FIXED_2,
            "fixed_3" to SwingMode.FIXED_3,
            "fixed_4" to SwingMode.FIXED_4,
            "fixed_5" to SwingMode.FIXED_5,
            "hada" to SwingMode.HADA,
            "fixed_horizontal_1" to SwingMode.FIXED_HORIZONTAL_1,
            "fixed_horizontal_2" to SwingMode.FIXED_HORIZONTAL_2,
            "fixed_horizontal_3" to SwingMode.FIXED_HORIZONTAL_3,
            "fixed_horizontal_4" to SwingMode.FIXED_HORIZONTAL_4,
            "fixed_horizontal_5" to SwingMode.FIXED_HORIZONTAL_5,
        )

    private val powerMode: PowerMode? by option(help = "Target power mode")
        .choice(
            "50" to PowerMode.POWER_50,
            "75" to PowerMode.POWER_75,
            "100" to PowerMode.POWER_100,
        )

    private val meritBMode: MeritBMode? by option(help = "Target Merit B mode")
        .choice(
            "fireplace_1" to MeritBMode.FIREPLACE_1,
            "fireplace_2" to MeritBMode.FIREPLACE_2,
            "off" to MeritBMode.OFF,
        )

    private val meritAMode: MeritAMode? by option(help = "Target Merit A mode")
        .choice(
            "high_power" to MeritAMode.HIGH_POWER,
            "cdu_silent_1" to MeritAMode.CDU_SILENT_1,
            "eco" to MeritAMode.ECO,
            "heating_8c" to MeritAMode.HEATING_8C,
            "sleep_care" to MeritAMode.SLEEP_CARE,
            "floor" to MeritAMode.FLOOR,
            "comfort" to MeritAMode.COMFORT,
            "cdu_silent_2" to MeritAMode.CDU_SILENT_2,
            "off" to MeritAMode.OFF,
        )

    private val pureIonMode: PureIonMode? by option(help = "Target Pure Ion mode")
        .choice(
            "on" to PureIonMode.ON,
            "off" to PureIonMode.OFF,
        )

    private val strongDefrost: Boolean? by option(help = "Target strong defrost setting (heat/auto mode only)")
        .choice(
            "on" to true,
            "off" to false,
        )

    private val defrostNow: Boolean by option(help = "Request on-demand defrost; requires --strong-defrost").flag()

    private val wifiLed: WifiLedStatus? by option(help = "Target Wi-Fi adapter LED status")
        .choice(
            "on" to WifiLedStatus.ON,
            "off" to WifiLedStatus.OFF,
        )

    private val scheduler: SchedulerStatus? by option(help = "Target weekly scheduler status")
        .choice(
            "on" to SchedulerStatus.ENABLED,
            "off" to SchedulerStatus.DISABLED,
        )

    private val smartSensing: SmartSensingStatus? by option(help = "Target smart sensing status")
        .choice(
            "on" to SmartSensingStatus.ON,
            "off" to SmartSensingStatus.OFF,
        )

    private val airDirection: AirDirection? by option(
        help = "Target smart sensing air direction: none, indirect, direct or a touch control area A0..E2",
    )
        .choice(
            buildMap {
                put("none", AirDirection.None)
                put("indirect", AirDirection.Indirect)
                put("direct", AirDirection.Direct)
                for (row in 0..4) {
                    for (column in 0..2) {
                        put("${'A' + row}$column", AirDirection.Area(row, column))
                    }
                }
            },
        )

    private val timer: Timer? by mutuallyExclusiveOptions(
        option("--off-timer", help = "Turn the AC off after the given delay (e.g., 30m, 2h, 1h 30m)")
            .convert { scheduledTimer(Duration.parse(it), Timer.Scheduled::Off) },
        option("--on-timer", help = "Turn the AC on after the given delay (e.g., 30m, 2h, 1h 30m)")
            .convert { scheduledTimer(Duration.parse(it), Timer.Scheduled::On) },
        option("--clear-timer", help = "Clear the timer")
            .flag()
            .convert { value ->
                when (value) {
                    true -> Timer.None
                    false -> null
                }
            },
    )
        .single()

    override fun run() {
        if (defrostNow && strongDefrost == null) {
            throw UsageError("--defrost-now requires --strong-defrost")
        }
        mutableContextBuilder += Command.SetParameters(
            acNames = acNames,
            fcuState = FCUState(
                acStatus = status,
                acMode = mode,
                temperature = temperature,
                fanMode = fanMode,
                swingMode = swingMode,
                powerMode = powerMode,
                meritBMode = meritBMode,
                meritAMode = meritAMode,
                pureIonMode = pureIonMode,
                indoorTemperature = null,
                outdoorTemperature = null,
                selfCleaningMode = null,
                errorCode = null,
                timer = timer,
                defrost = strongDefrost?.let { Defrost(strong = it, onDemand = defrostNow) },
                wifiLedStatus = wifiLed,
                schedulerStatus = scheduler,
                smartSensingStatus = smartSensing,
                airDirection = airDirection,
            ),
        )
    }
}

private fun scheduledTimer(delay: Duration, create: (Duration, LocalTime) -> Timer): Timer = create(delay, LocalTime.now(ZoneOffset.UTC))
