package org.example.parking.strategy;

import org.example.parking.model.ParkingFee;
import org.example.parking.model.ParkingTicket;

import java.util.Optional;

/**
 * Strategy for calculating parking rates.
 * Returns empty Optional if strategy doesn't apply.
 */
public interface RateStrategy {
    Optional<ParkingFee> calculateFee(ParkingTicket ticket);
    String name();
}

