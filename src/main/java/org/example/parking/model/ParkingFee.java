package org.example.parking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a calculated parking fee.
 * Immutable value object with monetary precision (2 decimal places).
 */
public class ParkingFee {
    private final BigDecimal amount;

    public ParkingFee(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Parking fee cannot be negative");
        }
        this.amount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public ParkingFee(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Parking fee cannot be negative");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double getAmountAsDouble() {
        return amount.doubleValue();
    }

    public boolean isLessThan(ParkingFee other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingFee that = (ParkingFee) o;
        return Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return String.format("$%.2f", amount);
    }
}

