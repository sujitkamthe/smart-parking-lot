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
 * Unit tests for NightOwlRateStrategy.
 */
class NightOwlRateStrategyTest {

    private TimeBasedFlatRateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TimeBasedFlatRateStrategy(
            "Night Owl Special",
            8.00,
            TimeRange.of(LocalTime.of(18, 0), LocalTime.of(23, 59, 59)),
            TimeRange.of(LocalTime.of(5, 0), LocalTime.of(10, 0)),
            18,
            ParkingTicket::isNextDay
        );
    }

    @Test
    void shouldApplyNightOwlRate_ValidTimes_Car() {
        // Entry 8 PM, Exit next day 7 AM (11 hours)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        assertEquals(8.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyLoyaltyDiscount_Silver() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.SILVER);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        // $8.00 - 10% = $7.20
        assertEquals(7.20, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyLoyaltyDiscount_Gold() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.GOLD);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        // $8.00 - 20% = $6.40
        assertEquals(6.40, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyLoyaltyDiscount_Platinum() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.PLATINUM);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        // $8.00 - 30% = $5.60
        assertEquals(5.60, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyVehicleMultiplier_Motorcycle() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        // $8.00 * 0.8 = $6.40
        assertEquals(6.40, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldApplyVehicleMultiplier_Bus() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.BUS, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        // $8.00 * 2.0 = $16.00
        assertEquals(16.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCombineVehicleMultiplierAndLoyalty() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.BUS, LoyaltyTier.GOLD);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        // $8.00 * 2.0 = $16.00, then - 20% = $12.80
        assertEquals(12.80, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldReject_EntryTooEarly() {
        // Entry 5 PM (before 6 PM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 17, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReject_ExitTooEarly() {
        // Exit 4 AM (before 5 AM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 4, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReject_ExitTooLate() {
        // Exit 11 AM (after 10 AM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 11, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReject_SameDay() {
        // Same day entry and exit
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 23, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReject_TwoDaysLater() {
        // Two days later
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 17, 7, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReject_DurationExceeds18Hours() {
        // Entry 6 PM, Exit next day 10 AM + 1 hour = 19 hours
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 18, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 13, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldAccept_ExactlyAtBoundaries() {
        // Entry 6 PM, Exit next day 10 AM (16 hours)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 18, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 10, 0);
        ParkingTicket ticket = new ParkingTicket(entry, exit, VehicleType.CAR, LoyaltyTier.NONE);

        Optional<ParkingFee> result = strategy.calculateFee(ticket);

        assertTrue(result.isPresent());
        assertEquals(8.00, result.get().getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldReturnCorrectStrategyName() {
        assertEquals("Night Owl Special", strategy.name());
    }
}

