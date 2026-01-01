package toshibaac.client.http

import toshibaac.client.DeviceUniqueId
import toshibaac.client.types.FCUState
import toshibaac.client.types.ProgramEntry

@JvmInline
public value class GetProgramSettingsResult(
    public val groupSettings: List<GroupSetting>,
) {
    public data class GroupSetting(
        val groupId: GroupId,
        val groupName: GroupName,
        val acSettings: List<ACSetting>,
        val programSetting: ProgramSetting,
    ) {
        public data class ACSetting(
            val id: ACId,
            val deviceUniqueId: DeviceUniqueId,
            val name: ACName,
            val model: ACModelId,
            val timeZone: String,
            val dstStatus: String,
            val schedulerStatus: String,
            val state: FCUState,
            val meritFeature: MeritFeature,
            val programSetting: ProgramSetting,
        )
    }

    public data class ProgramSetting(
        public val sunday: Program,
        public val monday: Program,
        public val tuesday: Program,
        public val wednesday: Program,
        public val thursday: Program,
        public val friday: Program,
        public val saturday: Program,
    ) {
        public data class Program(
            public val p1: ProgramEntry?,
            public val p2: ProgramEntry?,
            public val p3: ProgramEntry?,
            public val p4: ProgramEntry?,
            public val p5: ProgramEntry?,
            public val p6: ProgramEntry?,
            public val p7: ProgramEntry?,
            public val p8: ProgramEntry?,
            public val p9: ProgramEntry?,
            public val p10: ProgramEntry?,
        )
    }
}
