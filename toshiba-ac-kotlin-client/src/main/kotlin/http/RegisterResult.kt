package toshibaac.client.http

import toshibaac.client.DeviceId
import toshibaac.client.IoTHostName
import toshibaac.client.IoTSasToken

public data class RegisterResult(
    val deviceId: DeviceId,
    val hostName: IoTHostName,
    val primaryKey: PrimaryKey,
    val secondaryKey: SecondaryKey,
    val sasToken: IoTSasToken,
    val registerDate: RegisterDate,
)
