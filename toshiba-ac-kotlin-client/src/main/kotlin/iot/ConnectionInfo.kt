package toshibaac.client.iot

import toshibaac.client.DeviceId
import toshibaac.client.IoTHostName
import toshibaac.client.IoTSasToken

public data class ConnectionInfo(
    val hostName: IoTHostName,
    val deviceId: DeviceId,
    val sasToken: IoTSasToken,
)
