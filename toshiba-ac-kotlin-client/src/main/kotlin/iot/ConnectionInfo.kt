package toshibaac.client.iot

public data class ConnectionInfo(
    val hostName: String,
    val deviceId: String,
    val sasToken: String,
)
