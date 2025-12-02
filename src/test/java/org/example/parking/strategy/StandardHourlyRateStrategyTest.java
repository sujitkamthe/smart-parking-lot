package org.example.parking.strategy;

import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StandardHourlyRateStrategy.
 */
class StandardHourlyRateStrategyTest {

    private StandardHourlyRateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardHourlyRateStrategy();
    }

    @Test
    void shouldCalculateForExactOneHour_Car() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 11, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(5.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCalculateForExactTwoHours_Car() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 12, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // $5 + $3 = $8
        assertEquals(8.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCalculateForFiveHours_Car() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // $5 + $3 + $2 + $2 + $2 = $14
        assertEquals(14.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldRoundUpPartialHours_Car() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 11, 1); // 1 hour and 1 minute
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Rounds to 2 hours: $5 + $3 = $8
        assertEquals(8.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldRoundUpPartialHours_TwoAndHalf() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 12, 30); // 2.5 hours
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Rounds to 3 hours: $5 + $3 + $2 = $10
        assertEquals(10.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldHandleZeroTime_RoundsToOneHour() {
        LocalDateTime time = LocalDateTime.of(2024, 3, 15, 10, 0);
        ParkingTicket session = new ParkingTicket(time, time, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Minimum 1 hour
        assertEquals(5.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyMotorcycleMultiplier() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 13, 0); // 3 hours
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Base: $5 + $3 + $2 = $10
        // Motorcycle multiplier: 0.8
        // $10 * 0.8 = $8.00
        assertEquals(8.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyBusMultiplier() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 13, 0); // 3 hours
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.BUS);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Base: $5 + $3 + $2 = $10
        // Bus multiplier: 2.0
        // $10 * 2.0 = $20.00
        assertEquals(20.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldAlwaysReturnResult() {
        // Standard hourly rate is always applicable
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
    }

    @Test
    void shouldHandleLongStay_24Hours() {
        // Friday 10 AM - Saturday 10 AM (24 hours)
        // Friday has peak hours from 4 PM - 7 PM (hours 7, 8, 9 of parking)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 10, 0); // 24 hours
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Base: $5 + $3 + (22 * $2) = $52
        // Peak hours (4-7 PM): hours 7, 8, 9 are partially in peak
        // Hour 7 (4-5 PM): $2 * 1.5 = $3 (instead of $2, +$1)
        // Hour 8 (5-6 PM): $2 * 1.5 = $3 (instead of $2, +$1)
        // Hour 9 (6-7 PM): $2 * 1.5 = $3 (instead of $2, +$1)
        // Total: $52 + $3 = $55
        assertEquals(55.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("Standard Hourly Rate with Peak Hour Surcharge", strategy.name());
    }

    @Test
    void shouldApplyMorningPeakSurcharge() {
        // Monday 7 AM - 9 AM (2 hours in morning peak)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 7, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 9, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Hour 1 (7-8 AM): $5 * 1.5 = $7.50
        // Hour 2 (8-9 AM): $3 * 1.5 = $4.50
        // Total: $12.00
        assertEquals(12.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyEveningPeakSurcharge() {
        // Tuesday 4 PM - 7 PM (3 hours in evening peak)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 19, 16, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 19, 19, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Hour 1 (4-5 PM): $5 * 1.5 = $7.50
        // Hour 2 (5-6 PM): $3 * 1.5 = $4.50
        // Hour 3 (6-7 PM): $2 * 1.5 = $3.00
        // Total: $15.00
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldNotApplyPeakSurchargeOnWeekend() {
        // Saturday 7 AM - 9 AM (morning peak time but weekend)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 16, 7, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 9, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // No peak surcharge on weekends
        // Hour 1: $5, Hour 2: $3
        // Total: $8.00
        assertEquals(8.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyPeakSurchargePartialOverlap() {
        // Wednesday 9:30 AM - 11:30 AM (exactly 2 hours, partial overlap with morning peak)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 20, 9, 30);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 20, 11, 30);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Exactly 2 hours
        // Hour 1 (9:30-10:30 AM): overlaps with peak (until 10 AM) -> $5 * 1.5 = $7.50
        // Hour 2 (10:30-11:30 AM): no peak -> $3
        // Total: $10.50
        assertEquals(10.50, result.get().getAmountAsDouble(), 0.01);
    }
}

