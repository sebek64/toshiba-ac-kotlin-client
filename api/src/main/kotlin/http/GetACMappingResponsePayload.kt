package toshibaac.api.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class GetACMappingResponsePayload(
    val GroupId: String,
    val GroupName: String,
    val ConsumerId: String,
    val TimeZone: String,
    val ACList: List<ACMapping>,
) {
    @Serializable
    public data class ACMapping(
        val Id: String,
        val DeviceUniqueId: String,
        val Name: String,
        val ACModelId: String,
        val Description: String,
        val CreatedDate: String,
        val ACStateData: String,
        val FirmwareUpgradeStatus: String,
        val URL: String,
        val File: String,
        val MeritFeature: String,
        val AdapterType: String,
        val FirmwareVersion: String,
        val FirmwareCode: String,
        val ModeValues: List<ModeValue>,
        val ACOrder: JsonElement,
        val Control: JsonElement,
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
        val AutoReboot: JsonElement,
        val FilterCleaning: JsonElement,
        val VersionInfo: String,
        val FunctionSettingsSupport: FunctionSettingsSupportValue,
        val SilentNight: JsonElement,
        val Cdu: CduValue,
        val Fcu: FcuValue,
    ) {
        @Serializable
        public data class ModeValue(
            val Mode: String,
            val Temp: String,
            val FanSpeed: String,
        )

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

        @Serializable
        public data class CduValue(
            val model_name: JsonElement,
            val serial_number: JsonElement,
            val firmware_info: JsonElement,
            val eeprom_info: JsonElement,
        )

        @Serializable
        public data class FcuValue(
            val model_name: JsonElement,
            val serial_number: JsonElement,
            val firmware_info: JsonElement,
            val eeprom_info: JsonElement,
        )
    }
}
