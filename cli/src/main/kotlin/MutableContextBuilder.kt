package toshibaac.cli

import toshibaac.client.DeviceId
import toshibaac.client.http.Password
import toshibaac.client.http.Username

internal class MutableContextBuilder {
    data class Context(
        val username: Username,
        val password: Password,
        val deviceId: DeviceId,
        val commands: List<Command>,
    )

    private var username: Username? = null
    private var password: Password? = null
    private var deviceId: DeviceId? = null
    private val commands: MutableList<Command> = mutableListOf()

    fun setGlobals(
        username: Username,
        password: Password,
        deviceId: DeviceId,
    ) {
        this.username = username
        this.password = password
        this.deviceId = deviceId
    }

    operator fun plusAssign(cmd: Command) {
        commands += cmd
    }

    fun build() = Context(
        username = username!!,
        password = password!!,
        deviceId = deviceId!!,
        commands = commands.toList(),
    )
}
