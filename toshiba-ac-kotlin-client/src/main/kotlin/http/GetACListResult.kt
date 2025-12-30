package toshibaac.client.http

import toshibaac.client.DeviceUniqueId
import toshibaac.client.types.FCUState

@JvmInline
public value class GetACListResult(
    public val groups: List<Group>,
) {
    public data class Group(
        val groupId: GroupId,
        val groupName: GroupName,
        val consumerId: ConsumerId,
        val timeZone: GroupTimeZone,
        val acs: List<AC>,
    ) {
        public data class AC(
            val id: ACId,
            val deviceUniqueId: DeviceUniqueId,
            val name: ACName,
            val modelId: ACModelId,
            val description: ACDescription,
            val fcuState: FCUState,
            val meritFeature: MeritFeature,
            val adapterType: ACAdapterType,
        )
    }
}
