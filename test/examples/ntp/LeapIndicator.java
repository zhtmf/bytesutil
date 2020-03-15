package examples.ntp;

/**
 * This field indicates whether the last minute of the current day is to have a leap second applied. The field values follow:
 *  0: No leap second adjustment
 *  1: Last minute of the day has 61 seconds
 *  2: Last minute of the day has 59 seconds
 *  3: Clock is unsynchronized>
 * @author dzh
 */
public enum LeapIndicator {
    NO_ADJUSTMENT,
    LAST_MINUTE_61_SECONDS,
    LAST_MINUTE_659_SECONDS,
    CLOCK_UNSYNCHRONIZED
}
