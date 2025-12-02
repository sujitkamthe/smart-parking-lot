package org.example.parking.service;

import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;
import org.example.parking.model.TimeRange;
import org.example.parking.strategy.RateStrategy;
import org.example.parking.strategy.StandardHourlyRateStrategy;
import org.example.parking.strategy.TimeBasedFlatRateStrategy;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Calculates parking fees using multiple strategies, returning the lowest applicable rate.
 */
public record ParkingFeeCalculator(List<RateStrategy> strategies) {

    public ParkingFeeCalculator {
        Objects.requireNonNull(strategies, "Strategies cannot be null");
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one strategy required");
        }
        strategies = List.copyOf(strategies);
    }

    public static ParkingFeeCalculator withStandardStrategies() {
        return new ParkingFeeCalculator(List.of(
            new StandardHourlyRateStrategy(),
            new TimeBasedFlatRateStrategy(
                "Early Bird Special",
                15.00,
                TimeRange.of(LocalTime.of(6, 0), LocalTime.of(9, 0)),
                TimeRange.of(LocalTime.of(15, 30), LocalTime.of(19, 0)),
                15,
                ParkingTicket::isSameDay
            ),
            new TimeBasedFlatRateStrategy(
                "Night Owl Special",
                8.00,
                TimeRange.of(LocalTime.of(18, 0), LocalTime.of(23, 59, 59)),
                TimeRange.of(LocalTime.of(5, 0), LocalTime.of(10, 0)),
                18,
                ParkingTicket::isNextDay
            )
        ));
    }

    public ParkingFee calculateFee(ParkingTicket ticket) {
        Objects.requireNonNull(ticket, "Ticket cannot be null");

        return strategies.stream()
            .map(strategy -> strategy.calculateFee(ticket))
            .flatMap(Optional::stream)
            .min(Comparator.comparing(ParkingFee::getAmount))
            .orElseThrow(() -> new IllegalStateException("No applicable strategy found"));
    }

    public CalculationResult calculateWithDetails(ParkingTicket ticket) {
        Objects.requireNonNull(ticket, "Ticket cannot be null");

        var evaluations = strategies.stream()
            .map(strategy -> new RateEvaluation(
                strategy.name(),
                strategy.calculateFee(ticket).orElse(null)
            ))
            .toList();

        var lowestFee = evaluations.stream()
            .filter(e -> e.fee != null)
            .min(Comparator.comparing(e -> e.fee.getAmount()))
            .orElseThrow(() -> new IllegalStateException("No applicable strategy found"));

        return new CalculationResult(lowestFee.fee, lowestFee.strategyName);
    }

    public record CalculationResult(
        ParkingFee selectedFee,
        String selectedStrategy
    ) {}

    public record RateEvaluation(String strategyName, ParkingFee fee) {
        public boolean isApplicable() {
            return fee != null;
        }
    }
}

