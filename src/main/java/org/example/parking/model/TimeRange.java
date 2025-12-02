package org.example.parking.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a time range with a start and end.
 * Can be used for both LocalTime (time of day) and LocalDateTime (specific date-time).
 */
public record TimeRange<T>(T start, T end) {

    public TimeRange {
        Objects.requireNonNull(start, "Start time cannot be null");
        Objects.requireNonNull(end, "End time cannot be null");
    }

    /**
     * Factory method for creating a time range with LocalTime.
     */
    public static TimeRange<LocalTime> of(LocalTime start, LocalTime end) {
        return new TimeRange<>(start, end);
    }

    /**
     * Factory method for creating a time range with LocalDateTime.
     */
    public static TimeRange<LocalDateTime> of(LocalDateTime start, LocalDateTime end) {
        return new TimeRange<>(start, end);
    }

    /**
     * Checks if the given time falls within this time range (inclusive).
     * Only works for LocalTime ranges.
     */
    public boolean contains(LocalTime time) {
        if (!(start instanceof LocalTime)) {
            throw new UnsupportedOperationException("contains() only works for LocalTime ranges");
        }
        return !time.isBefore((LocalTime) start) && !time.isAfter((LocalTime) end);
    }

    /**
     * Checks if this time range overlaps with another LocalDateTime range.
     * Uses inclusive start and exclusive end for both ranges.
     */
    public boolean overlaps(TimeRange<LocalDateTime> other) {
        if (!(start instanceof LocalDateTime) || !(other.start instanceof LocalDateTime)) {
            throw new UnsupportedOperationException("overlaps() only works for LocalDateTime ranges");
        }

        var thisStart = (LocalDateTime) start;
        var thisEnd = (LocalDateTime) end;
        var otherStart = other.start;
        var otherEnd = other.end;

        var thisStartTime = thisStart.toLocalTime();
        var thisEndTime = thisEnd.toLocalTime();
        var targetStart = otherStart.toLocalTime();
        var targetEnd = otherEnd.toLocalTime();

        // Handle midnight crossing for the hour segment
        if (thisEndTime.isBefore(thisStartTime) || thisEndTime.equals(thisStartTime)) {
            // Hour crosses midnight - check both days
            return !thisStartTime.isAfter(targetEnd) || thisEndTime.isAfter(targetStart);
        }

        // Normal case: check for overlap (start inclusive, end exclusive)
        return thisStartTime.isBefore(targetEnd) && thisEndTime.isAfter(targetStart);
    }

    /**
     * Checks if a LocalDateTime range overlaps with a LocalTime range (target).
     * This is useful for checking if a specific hour overlaps with peak hours.
     */
    public boolean overlapsTimeRange(LocalTime targetStart, LocalTime targetEnd) {
        if (!(start instanceof LocalDateTime)) {
            throw new UnsupportedOperationException("overlapsTimeRange() only works for LocalDateTime ranges");
        }

        var startTime = ((LocalDateTime) start).toLocalTime();
        var endTime = ((LocalDateTime) end).toLocalTime();

        // Handle midnight crossing for the hour segment
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            // Hour crosses midnight - check both days
            return !startTime.isAfter(targetEnd) || endTime.isAfter(targetStart);
        }

        // Normal case: check for overlap (start inclusive, end exclusive)
        return startTime.isBefore(targetEnd) && endTime.isAfter(targetStart);
    }
}

