package org.example.parking.service;

import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ParkingFeeCalculator.
 * Tests the complete flow with multiple strategies.
 */
class ParkingFeeCalculatorTest {

    private ParkingFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = ParkingFeeCalculator.withStandardStrategies();
    }

    // ========== Standard Hourly Rate Tests ==========

    @Test
    void shouldCalculateStandardRate_Car_5Hours() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // $5 + $3 + $2 + $2 + $2 = $14
        assertEquals(14.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCalculateStandardRate_Motorcycle_4Hours() {
        // Friday 2 PM to 6 PM (4 hours, hours 3-4 overlap with evening peak 4-7 PM)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 14, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 18, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        ParkingFee fee = calculator.calculateFee(session);

        // Hour 1 (2-3 PM): $5
        // Hour 2 (3-4 PM): $3
        // Hour 3 (4-5 PM): $2 * 1.5 = $3 (peak)
        // Hour 4 (5-6 PM): $2 * 1.5 = $3 (peak)
        // Base: $5 + $3 + $3 + $3 = $14
        // Motorcycle: $14 * 0.8 = $11.20
        assertEquals(11.20, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCalculateStandardRate_Bus_3Hours() {
        // Friday 1 PM to 4 PM (3 hours, no peak hours)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 13, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 16, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.BUS);

        ParkingFee fee = calculator.calculateFee(session);

        // Base: $5 + $3 + $2 = $10 (no peak overlap)
        // Bus: $10 * 2.0 = $20.00
        assertEquals(20.00, fee.getAmountAsDouble(), 0.01);
    }

    // ========== Early Bird Special Tests ==========

    @Test
    void shouldCalculateEarlyBird_Car_CheaperThanStandard() {
        // Monday 8 AM to 5 PM (9 hours)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Early Bird: $15 (cheaper than standard ~$21)
        assertEquals(15.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCalculateEarlyBird_Motorcycle() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        ParkingFee fee = calculator.calculateFee(session);

        // Early Bird: $15 * 0.8 = $12.00
        assertEquals(12.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldNotApplyEarlyBird_ExitTooLate_UseStandard() {
        // Monday 8 AM to 7:01 PM (11+ hours, exit too late for Early Bird)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 19, 1);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Rounds to 12 hours
        // Hours 1-2 (8-10 AM): peak -> $5*1.5 + $3*1.5 = $12
        // Hours 3-9 (10 AM - 4 PM): no peak -> $2*7 = $14
        // Hours 10-12 (4-7 PM): peak -> $2*1.5*3 = $9
        // Total: $12 + $14 + $9 = $35
        assertEquals(35.00, fee.getAmountAsDouble(), 0.01);
    }

    // ========== Night Owl Special Tests ==========

    @Test
    void shouldCalculateNightOwl_ValidScenario() {
        // Entry 8 PM Friday, Exit 7 AM Saturday (11 hours)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Night Owl: $8 (cheaper than standard ~$26)
        assertEquals(8.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldCalculateNightOwl_Motorcycle() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 7, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        ParkingFee fee = calculator.calculateFee(session);

        // Night Owl: $8 * 0.8 = $6.40
        assertEquals(6.40, fee.getAmountAsDouble(), 0.01);
    }

    // ========== Multiple Strategies - Choose Lowest ==========

    @Test
    void shouldChooseLowest_StandardVsPeakHours() {
        // Monday 8 AM to 10 AM (2 hours in morning peak)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 10, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Standard with peak: $5*1.5 + $3*1.5 = $12
        assertEquals(12.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldChooseLowest_WeekendNoSurcharge() {
        // Saturday 2 PM to 2:30 PM (30 minutes, no special rates apply)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 16, 14, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 14, 30);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        ParkingFee fee = calculator.calculateFee(session);

        // Standard: $5 * 0.8 = $4 (no peak surcharge on weekends)
        assertEquals(4.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldChooseLowest_LongStayStandardOnly() {
        // Saturday 9 AM to Sunday 9 PM (36 hours, only standard applies)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 16, 9, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 17, 21, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Standard: $5 + $3 + (34 * $2) = $76 (no peak on weekends)
        assertEquals(76.00, fee.getAmountAsDouble(), 0.01);
    }

    // ========== Edge Cases ==========

    @Test
    void shouldHandleMidnightCrossing() {
        // Monday 11:30 PM to Tuesday 12:30 AM (1 hour)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 23, 30);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 19, 0, 30);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Standard rate: 1 hour = $5
        assertEquals(5.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldHandleExactHourBoundary() {
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 11, 0, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        assertEquals(5.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldRoundUpPartialHour() {
        // 2 hours and 1 minute
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 15, 12, 1);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Rounds to 3 hours: $5 + $3 + $2 = $10
        assertEquals(10.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void shouldThrowException_NullSession() {
        assertThrows(NullPointerException.class, () -> calculator.calculateFee(null));
    }

    @Test
    void shouldThrowException_NoStrategies() {
        assertThrows(IllegalArgumentException.class,
            () -> new ParkingFeeCalculator(List.of()));
    }

    // ========== Detailed Calculation Tests ==========

    @Test
    void shouldReturnDetailedCalculation() {
        // Saturday 8 AM to 5 PM (9 hours) - Early Bird applies
        LocalDateTime entry = LocalDateTime.of(2024, 3, 16, 8, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 17, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFeeCalculator.CalculationResult result = calculator.calculateWithDetails(session);

        assertNotNull(result);
        assertEquals(15.00, result.selectedFee().getAmountAsDouble(), 0.01);
        assertEquals("Early Bird Special", result.selectedStrategy());
        assertEquals(3, result.allEvaluations().size());
    }

    @Test
    void shouldShowAllStrategiesInDetails() {
        // Monday 10 AM to 1 PM (3 hours) - only standard applies
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 13, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFeeCalculator.CalculationResult result = calculator.calculateWithDetails(session);

        // Standard should apply, others should not
        long applicableStrategies = result.allEvaluations().stream()
            .filter(ParkingFeeCalculator.RateEvaluation::isApplicable)
            .count();

        assertEquals(1, applicableStrategies);
        assertEquals("Standard Hourly Rate with Peak Hour Surcharge", result.selectedStrategy());
    }

    // ========== Real-world Scenarios ==========

    @Test
    void scenario_DailyCommuter() {
        // Typical commuter: Monday 7:30 AM to 5:30 PM
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 7, 30);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 18, 17, 30);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Early Bird applies: $15
        assertEquals(15.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void scenario_WeekendShopper() {
        // Weekend shopper: Saturday 2 PM to 6 PM (4 hours, no special rates)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 16, 14, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 16, 18, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Standard (no peak on weekends): $5 + $3 + $2 + $2 = $12
        assertEquals(12.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void scenario_QuickErrand() {
        // Quick errand: Wednesday 3 PM to 3:15 PM (15 minutes)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 20, 15, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 20, 15, 15);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Rounds to 1 hour: $5 (no peak overlap)
        assertEquals(5.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void scenario_OvernightParking() {
        // Monday 8 PM to Tuesday 8 AM (12 hours) - Night Owl applies
        LocalDateTime entry = LocalDateTime.of(2024, 3, 18, 20, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 3, 19, 8, 0);
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Night Owl: $8 (cheaper than standard ~$28 with peak)
        assertEquals(8.00, fee.getAmountAsDouble(), 0.01);
    }

    @Test
    void scenario_WeekendGetaway() {
        // Friday night to Sunday afternoon (3 days but starts on Friday)
        LocalDateTime entry = LocalDateTime.of(2024, 3, 15, 22, 0); // Friday
        LocalDateTime exit = LocalDateTime.of(2024, 3, 17, 14, 0); // Sunday
        ParkingTicket session = new ParkingTicket(entry, exit, VehicleType.CAR);

        ParkingFee fee = calculator.calculateFee(session);

        // Only standard applies (40 hours, no special rates for multi-day)
        // Friday 10 PM - Sunday 2 PM = 40 hours
        // No peak hours involved (starts after 7 PM, weekend has no peak)
        // $5 + $3 + (38 * $2) = $84
        assertEquals(84.00, fee.getAmountAsDouble(), 0.01);
    }
}

