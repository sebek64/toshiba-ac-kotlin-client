package toshibaac.cli

import com.github.ajalt.clikt.core.CliktCommand
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
import toshibaac.client.types.FCUState
import toshibaac.client.types.FanMode
import toshibaac.client.types.MeritAMode
import toshibaac.client.types.MeritBMode
import toshibaac.client.types.PowerMode
import toshibaac.client.types.PureIonMode
import toshibaac.client.types.SelfCleaningMode
import toshibaac.client.types.SwingMode
import toshibaac.client.types.Temperature
import kotlin.concurrent.thread
import kotlin.time.Duration

public suspend fun main(args: Array<String>) {
    val mutableContextBuilder = MutableContextBuilder()
    ToshibaCLICommand(mutableContextBuilder)
        .subcommands(
            StatusCommand(mutableContextBuilder),
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
                    IoTClientWrapper.create(
                        deviceClient = deviceClient.get(),
                        deviceId = context.deviceId,
                    )
                }.use { iotClientWrapper ->
                    context.commands.forEach { command ->
                        command.execute(
                            deviceClient = deviceClient,
                            iotClientWrapper = iotClientWrapper,
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

    private val temperature: Temperature? by option(help = "Target temperature in Celsius")
        .convert { Temperature(it.toInt()) }

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

    private val selfCleaningMode: SelfCleaningMode? by option(help = "Target Self Cleaning mode")
        .choice(
            "on" to SelfCleaningMode.ON,
            "off" to SelfCleaningMode.OFF,
        )

    override fun run() {
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
                selfCleaningMode = selfCleaningMode,
            ),
        )
    }
}
