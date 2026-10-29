package toshibaac.client.types

public data class AirQuality(
    public val level: Level?,
    /** µg/m³ */
    public val pm25: Int?,
    /** µg/m³ */
    public val pm10: Int?,
    /** ppm */
    public val co2: Int?,
) {
    public enum class Level {
        GOOD,
        MODERATE,
        UNHEALTHY,
        VERY_UNHEALTHY,
    }
}
