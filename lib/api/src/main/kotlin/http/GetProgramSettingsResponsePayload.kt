package toshibaac.api.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class GetProgramSettingsResponsePayload(
    val ConsumerId: String,
    val ACGroupProgramSettings: List<ACGroupProgramSetting>,
) {
    @Serializable
    public data class ACGroupProgramSetting(
        val ACProgramSettingList: List<ACProgramSetting>,
        val ConsumerId: String,
        val PartitionKey: JsonElement,
        val GroupId: String,
        val GroupName: String,
        val Type: JsonElement,
        val programSetting: ProgramSetting,
        val time: JsonElement,
        val dst: DST,
    )

    @Serializable
    public data class ACProgramSetting(
        val ACId: String,
        val ConsumerId: JsonElement,
        val ACUniqueId: String,
        val ACName: String,
        val ACModel: String,
        val Type: JsonElement,
        val dstStatus: String,
        val timeZone: String,
        val schedulerStatus: String,
        val ACStateDataForProgram: String,
        val MeritFeature: String,
        val PartitionKey: JsonElement,
        val FlapSetting: JsonElement,
        val OpeMode: JsonElement,
        val Cool_temp_max: JsonElement,
        val Cool_temp_min: JsonElement,
        val Heat_temp_max: JsonElement,
        val Heat_temp_min: JsonElement,
        val Dry_temp_max: JsonElement,
        val Dry_temp_min: JsonElement,
        val Auto_temp_max: JsonElement,
        val Auto_temp_min: JsonElement,
        val Frost_protect_temp: JsonElement,
        val SystemConfig: JsonElement,
        val FunctionSettingsSupport: FunctionSettingsSupportValue,
        val programSetting: ProgramSetting,
        val time: JsonElement,
        val dst: DST,
    ) {
        @Serializable
        public data class FunctionSettingsSupportValue(
            val PowerSelSupport: Boolean,
            val PureSupport: Boolean,
            val AutoRebootSupport: Boolean,
            val FilterCleaningSupport: Boolean,
            val DeforstSupport: Boolean,
            val SelfCalHeatQuantitySupport: Boolean,
            val CalHeatQuantityOnColudSupport: Boolean,
            val FixGrillSupport: Boolean,
            val NumberProgramSupport: Int,
        )
    }

    @Serializable
    public data class ProgramSetting(
        val Sunday: Program,
        val Monday: Program,
        val Tuesday: Program,
        val Wednesday: Program,
        val Thursday: Program,
        val Friday: Program,
        val Saturday: Program,
    ) {
        @Serializable
        public data class Program(
            val p1: String,
            val p2: String,
            val p3: String,
            val p4: String,
            val p5: String,
            val p6: String,
            val p7: String,
            val p8: String,
            val p9: String,
            val p10: String,
        )
    }

    @Serializable
    public data class DST(
        val Time: String,
        val Status: String,
    )
}
