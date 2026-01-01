package toshibaac.api.http

import kotlinx.serialization.Serializable

@Serializable
public data class RegisterResponsePayload(
    val DeviceId: String,
    val HostName: String,
    val PrimaryKey: String,
    val SecondaryKey: String,
    val SasToken: String,
    val RegisterDate: String,
)
