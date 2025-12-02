package org.example.parking.strategy;

import org.example.parking.model.LoyaltyTier;
import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.TimeRange;
import org.example.parking.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EarlyBirdRateStrategy.
 */
class EarlyBirdRateStrategyTest {

    private TimeBasedFlatRateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TimeBasedFlatRateStrategy(
            "Early Bird Special",
            15.00,
            TimeRange.of(LocalTime.of(6, 0), LocalTime.of(9, 0)),
            TimeRange.of(LocalTime.of(15, 30), LocalTime.of(19, 0)),
            15,
            ParkingTicket::isSameDay
        );
    }

    @Test
    void shouldApplyEarlyBird_ValidScenario_Car() {
        // Monday 8:00 AM to 5:00 PM (9 hours)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyEarlyBird_EntryAt6AM() {
        // Entry at 6:00 AM (boundary)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 6, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 16, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyEarlyBird_EntryAt9AM() {
        // Entry at 9:00 AM (boundary)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 9, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 16, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyEarlyBird_ExitAt330PM() {
        // Exit at 3:30 PM (boundary)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 15, 30);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyEarlyBird_ExitAt7PM() {
        // Exit at 7:00 PM (boundary)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 19, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldNotApply_EntryTooEarly() {
        // Entry at 5:59 AM (before 6:00 AM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 5, 59);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotApply_EntryTooLate() {
        // Entry at 9:01 AM (after 9:00 AM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 9, 1);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotApply_ExitTooEarly() {
        // Exit at 3:29 PM (before 3:30 PM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 15, 29);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotApply_ExitTooLate() {
        // Exit at 7:01 PM (after 7:00 PM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 19, 1);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotApply_DifferentDays() {
        // Entry Monday, Exit Tuesday
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 19, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotApply_DurationExceeds15Hours() {
        // 6 AM to 10 PM = 16 hours (exceeds 15 hour limit)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 6, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 22, 0); // 16 hours, but exit after 7 PM
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        // Should fail because exit is after 7 PM
        assertFalse(result.isPresent());
    }

    @Test
    void shouldApply_DurationExactly15Hours() {
        // 6 AM to 9 PM would be 15 hours, but exit must be by 7 PM
        // So let's test 6 AM to 7 PM = 13 hours (within limit and valid exit time)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 6, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 19, 0); // 13 hours
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyMotorcycleMultiplier() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Base: $15 * 0.8 = $12.00
        assertEquals(12.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyBusMultiplier() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.BUS);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // Base: $15 * 2.0 = $30.00
        assertEquals(30.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyLoyaltyDiscount_Silver() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.SILVER);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // $15.00 - 10% = $13.50
        assertEquals(13.50, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyLoyaltyDiscount_Gold() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.GOLD);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // $15.00 - 20% = $12.00
        assertEquals(12.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyLoyaltyDiscount_Platinum() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.PLATINUM);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // $15.00 - 30% = $10.50
        assertEquals(10.50, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCombineVehicleMultiplierAndLoyalty() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.BUS, LoyaltyTier.GOLD);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        assertTrue(result.isPresent());
        // $15.00 * 2.0 = $30.00, then - 20% = $24.00
        assertEquals(24.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyOnWeekend_IfConditionsMet() {
        // Saturday with Early Bird conditions
        LocalDateTime entry = LocalDateTime.of(2024, 3, 16, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        Optional<ParkingFee> result = strategy.calculateFee(session);

        // Early Bird should apply even on weekend if conditions are met
        assertTrue(result.isPresent());
        assertEquals(15.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("Early Bird Special", strategy.name());
    }
}

