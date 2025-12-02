package org.example.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a parking ticket with entry and exit information.
 * Using Java record for immutable value semantics.
 */
public record ParkingTicket(
    LocalDateTime entryTime,
    LocalDateTime exitTime,
    VehicleType vehicleType,
    LoyaltyTier loyaltyTier
) {
    public ParkingTicket {
        Objects.requireNonNull(entryTime, "Entry time cannot be null");
        Objects.requireNonNull(exitTime, "Exit time cannot be null");
        Objects.requireNonNull(vehicleType, "Vehicle type cannot be null");
        Objects.requireNonNull(loyaltyTier, "Loyalty tier cannot be null");

        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
    }

    /**
     * Convenience constructor with default NONE loyalty tier.
     */
    public ParkingTicket(LocalDateTime entryTime, LocalDateTime exitTime, VehicleType vehicleType) {
        this(entryTime, exitTime, vehicleType, LoyaltyTier.NONE);
    }

    /**
     * Calculates parking duration in hours, rounded up.
     * Minimum 1 hour.
     */
    public long roundedHours() {
        var duration = Duration.between(entryTime, exitTime);
        return Math.max(1, (duration.toSeconds() + 3599) / 3600);
    }

    /**
     * Calculates exact parking duration in hours (not rounded).
     */
    public long durationHours() {
        return Duration.between(entryTime, exitTime).toHours();
    }

    /**
     * Checks if entry and exit are on the same calendar day.
     */
    public boolean isSameDay() {
        return entryTime.toLocalDate().equals(exitTime.toLocalDate());
    }

    /**
     * Checks if exit is on the next consecutive calendar day after entry.
     */
    public boolean isNextDay() {
        return exitTime.toLocalDate().equals(entryTime.toLocalDate().plusDays(1));
    }

    /**
     * Returns the parking period as a TimeRange.
     */
    public TimeRange<LocalDateTime> parkingPeriod() {
        return TimeRange.of(entryTime, exitTime);
    }
}

