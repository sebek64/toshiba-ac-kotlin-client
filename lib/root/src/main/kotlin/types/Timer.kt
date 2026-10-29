package toshibaac.client.types

import java.time.LocalTime
import kotlin.time.Duration

public sealed interface Timer {
    /** When sent, clears the timer. */
    public data object None : Timer

    /** Fires [delay] after [setAt], the UTC time of day when the timer was set. */
    public sealed interface Scheduled : Timer {
        public val delay: Duration
        public val setAt: LocalTime

        /** Turns the AC off. */
        public data class Off(override val delay: Duration, override val setAt: LocalTime) : Scheduled

        /** Turns the AC on. */
        public data class On(override val delay: Duration, override val setAt: LocalTime) : Scheduled

        /** Reported by the AC only, never sent. */
        public data class OnOff(override val delay: Duration, override val setAt: LocalTime) : Scheduled
    }
}
