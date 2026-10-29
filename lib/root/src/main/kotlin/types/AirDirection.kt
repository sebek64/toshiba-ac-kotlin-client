package toshibaac.client.types

public sealed interface AirDirection {
    public data object None : AirDirection

    /** Avoids blowing on people. */
    public data object Indirect : AirDirection

    /** Blows towards people. */
    public data object Direct : AirDirection

    /** Touch control: blows towards an area of the room grid, rows A–E (0–4) and columns 0–2. */
    public data class Area(public val row: Int, public val column: Int) : AirDirection {
        init {
            require(row in 0..4) { "Row must be in 0..4, was $row" }
            require(column in 0..2) { "Column must be in 0..2, was $column" }
        }
    }
}
