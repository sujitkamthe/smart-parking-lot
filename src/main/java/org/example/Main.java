package org.example;

import org.example.parking.model.LoyaltyTier;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.VehicleType;
import org.example.parking.service.ParkingFeeCalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Demo application showing the parking fee calculator with new pricing policies.
 */
public class Main {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        System.out.println("=== Smart Parking Lot Rate Calculator ===");
        System.out.println("New Pricing Policies:");
        System.out.println("1. Standard Hourly with Peak Hour Surcharge (7-10 AM, 4-7 PM weekdays)");
        System.out.println("2. Early Bird Special ($15, 6-9 AM entry, 3:30-7 PM exit, max 15 hours)");
        System.out.println("3. Night Owl Special ($8, 6 PM-midnight entry, 5-10 AM exit next day, max 18 hours)");
        System.out.println("   * Early Bird & Night Owl support loyalty discounts\n");

        var calculator = ParkingFeeCalculator.withStandardStrategies();

        demo(calculator, "Standard - Off-Peak 5 hours",
            "2024-03-15 10:00", "2024-03-15 15:00", VehicleType.CAR, LoyaltyTier.NONE);

        demo(calculator, "Standard - With Morning Peak",
            "2024-03-18 08:00", "2024-03-18 10:00", VehicleType.CAR, LoyaltyTier.NONE);

        demo(calculator, "Standard - With Evening Peak",
            "2024-03-19 16:00", "2024-03-19 19:00", VehicleType.CAR, LoyaltyTier.NONE);

        demo(calculator, "Early Bird - No Loyalty",
            "2024-03-18 08:00", "2024-03-18 17:00", VehicleType.CAR, LoyaltyTier.NONE);

        demo(calculator, "Early Bird - Gold Member",
            "2024-03-18 08:00", "2024-03-18 17:00", VehicleType.CAR, LoyaltyTier.GOLD);

        demo(calculator, "Night Owl - No Loyalty",
            "2024-03-15 20:00", "2024-03-16 07:00", VehicleType.CAR, LoyaltyTier.NONE);

        demo(calculator, "Night Owl - Platinum Member",
            "2024-03-15 20:00", "2024-03-16 07:00", VehicleType.CAR, LoyaltyTier.PLATINUM);

        demo(calculator, "Motorcycle - 4 hours with peak",
            "2024-03-15 14:00", "2024-03-15 18:00", VehicleType.MOTORCYCLE, LoyaltyTier.NONE);

        demo(calculator, "Bus - Weekend (no peak)",
            "2024-03-16 10:00", "2024-03-16 15:00", VehicleType.BUS, LoyaltyTier.NONE);
    }

    private static void demo(ParkingFeeCalculator calculator, String desc,
                            String entry, String exit, VehicleType vehicle, LoyaltyTier loyalty) {
        var entryTime = LocalDateTime.parse(entry, FMT);
        var exitTime = LocalDateTime.parse(exit, FMT);
        var ticket = new ParkingTicket(entryTime, exitTime, vehicle, loyalty);
        var result = calculator.calculateWithDetails(ticket);

        System.out.printf("%s%n  %s -> %s (%s, %s)%n  Fee: $%.2f (%s)%n",
            desc, entry, exit, vehicle, loyalty,
            result.selectedFee().getAmountAsDouble(), result.selectedStrategy());

    }
}
