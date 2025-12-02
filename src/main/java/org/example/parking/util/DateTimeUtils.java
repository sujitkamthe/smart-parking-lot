package org.example.parking.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Utility methods for date and time operations.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if the given date-time falls on a weekday (Monday-Friday).
     *
     * @param dateTime the date-time to check
     * @return true if it's a weekday, false if it's a weekend
     */
    public static boolean isWeekday(LocalDateTime dateTime) {
        var dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}

