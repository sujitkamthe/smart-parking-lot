package org.example.parking.strategy;

import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.TimeRange;

import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A configurable time-based flat rate strategy that checks entry/exit time ranges,
 * duration limits, and day constraints.
 * Supports vehicle type multipliers and loyalty tier discounts.
 */
public record TimeBasedFlatRateStrategy(
    String name,
    double baseCarFee,
    TimeRange<LocalTime> entryTimeRange,
    TimeRange<LocalTime> exitTimeRange,
    long maxDurationHours,
    Predicate<ParkingTicket> dayConstraint
) implements RateStrategy {

    @Override
    public Optional<ParkingFee> calculateFee(ParkingTicket ticket) {
        if (!isEligible(ticket)) {
            return Optional.empty();
        }

        // Apply vehicle multiplier first, then loyalty discount
        var baseAmount = baseCarFee * ticket.vehicleType().getRateMultiplier();
        var finalAmount = ticket.loyaltyTier().applyDiscount(baseAmount);

        return Optional.of(new ParkingFee(finalAmount));
    }

    private boolean isEligible(ParkingTicket ticket) {
        var entryTime = ticket.entryTime().toLocalTime();
        var exitTime = ticket.exitTime().toLocalTime();

        return ticket.durationHours() <= maxDurationHours
                && dayConstraint.test(ticket)
                && entryTimeRange.contains(entryTime)
                && exitTimeRange.contains(exitTime);
    }
}

