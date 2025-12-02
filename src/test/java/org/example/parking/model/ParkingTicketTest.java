package org.example.parking.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParkingTicketTest {

    @Test
    void shouldCreateValidTicket() {
        var entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        var exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        var ticket = new ParkingTicket(entry, exit, VehicleType.CAR);

        assertEquals(entry, ticket.entryTime());
        assertEquals(exit, ticket.exitTime());
        assertEquals(VehicleType.CAR, ticket.vehicleType());
    }

    @Test
    void shouldRejectNullEntryTime() {
        var exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        assertThrows(NullPointerException.class,
            () -> new ParkingTicket(null, exit, VehicleType.CAR));
    }

    @Test
    void shouldRejectNullExitTime() {
        var entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        assertThrows(NullPointerException.class,
            () -> new ParkingTicket(entry, null, VehicleType.CAR));
    }

    @Test
    void shouldRejectNullVehicleType() {
        var entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        var exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        assertThrows(NullPointerException.class,
            () -> new ParkingTicket(entry, exit, null));
    }

    @Test
    void shouldRejectExitBeforeEntry() {
        var entry = LocalDateTime.of(2024, 3, 15, 15, 0);
        var exit = LocalDateTime.of(2024, 3, 15, 10, 0);
        assertThrows(IllegalArgumentException.class,
            () -> new ParkingTicket(entry, exit, VehicleType.CAR));
    }

    @Test
    void shouldAllowSameEntryAndExitTime() {
        var time = LocalDateTime.of(2024, 3, 15, 10, 0);
        var ticket = new ParkingTicket(time, time, VehicleType.CAR);
        assertEquals(time, ticket.entryTime());
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        var entry = LocalDateTime.of(2024, 3, 15, 10, 0);
        var exit = LocalDateTime.of(2024, 3, 15, 15, 0);
        var ticket1 = new ParkingTicket(entry, exit, VehicleType.CAR);
        var ticket2 = new ParkingTicket(entry, exit, VehicleType.CAR);
        var ticket3 = new ParkingTicket(entry, exit, VehicleType.MOTORCYCLE);

        assertEquals(ticket1, ticket2);
        assertNotEquals(ticket1, ticket3);
    }
}

