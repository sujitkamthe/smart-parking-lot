package org.example.parking.strategy;

import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.TimeRange;
import org.example.parking.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.LongStream;

/**
 * Standard hourly rate strategy - always applicable.
 * Progressive pricing: $5 (1st hour), $3 (2nd hour), $2 (each additional hour) for cars.
 * Peak hour surcharge: 1.5x multiplier for hours overlapping 7-10 AM or 4-7 PM on weekdays.
 */
public record StandardHourlyRateStrategy() implements RateStrategy {

    private static final double FIRST_HOUR_RATE = 5.00;
    private static final double SECOND_HOUR_RATE = 3.00;
    private static final double ADDITIONAL_HOUR_RATE = 2.00;
    private static final double PEAK_HOUR_MULTIPLIER = 1.5;

    private static final TimeRange<LocalTime> MORNING_PEAK = TimeRange.of(LocalTime.of(7, 0), LocalTime.of(10, 0));
    private static final TimeRange<LocalTime> EVENING_PEAK = TimeRange.of(LocalTime.of(16, 0), LocalTime.of(19, 0));

    @Override
    public Optional<ParkingFee> calculateFee(ParkingTicket ticket) {
        var hours = ticket.roundedHours();

        var totalAmount = LongStream.rangeClosed(1, hours)
                .mapToDouble(hour -> calculateHourlyRate((int) hour, ticket))
                .sum();

        return Optional.of(new ParkingFee(totalAmount * ticket.vehicleType().getRateMultiplier()));
    }

    private double calculateHourlyRate(int hourNumber, ParkingTicket ticket) {
        var hourStart = ticket.entryTime().plusHours(hourNumber - 1);
        var hourSegment = TimeRange.of(hourStart, hourStart.plusHours(1));
        var baseRate = rateForHour(hourNumber);

        return isPeakHour(hourSegment)
                ? baseRate * PEAK_HOUR_MULTIPLIER
                : baseRate;
    }

    private double rateForHour(int hour) {
        return switch (hour) {
            case 1 -> FIRST_HOUR_RATE;
            case 2 -> SECOND_HOUR_RATE;
            default -> ADDITIONAL_HOUR_RATE;
        };
    }

    /**
     * Checks if an hour segment overlaps with peak hours (7-10 AM or 4-7 PM on weekdays).
     * Even partial overlap triggers peak pricing for that hour.
     */
    private boolean isPeakHour(TimeRange<LocalDateTime> hourSegment) {
        return DateTimeUtils.isWeekday(hourSegment.start())
                && (hourSegment.overlapsTimeRange(MORNING_PEAK.start(), MORNING_PEAK.end())
                    || hourSegment.overlapsTimeRange(EVENING_PEAK.start(), EVENING_PEAK.end()));
    }


    @Override
    public String name() {
        return "Standard Hourly Rate with Peak Hour Surcharge";
    }
}

