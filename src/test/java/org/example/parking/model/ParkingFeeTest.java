package org.example.parking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParkingFee model.
 */
class ParkingFeeTest {

    @Test
    void shouldCreateValidFeeFromDouble() {
        ParkingFee fee = new ParkingFee(15.50);

        assertEquals(15.50, fee.getAmountAsDouble(), 0.001);
        assertEquals(new BigDecimal("15.50"), fee.getAmount());
    }

    @Test
    void shouldCreateValidFeeFromBigDecimal() {
        ParkingFee fee = new ParkingFee(new BigDecimal("20.75"));

        assertEquals(20.75, fee.getAmountAsDouble(), 0.001);
        assertEquals(new BigDecimal("20.75"), fee.getAmount());
    }

    @Test
    void shouldRoundToTwoDecimalPlaces() {
        ParkingFee fee = new ParkingFee(15.556);

        assertEquals(15.56, fee.getAmountAsDouble(), 0.001);
    }

    @Test
    void shouldRejectNegativeFee() {
        assertThrows(IllegalArgumentException.class, () -> new ParkingFee(-5.00));
    }

    @Test
    void shouldAllowZeroFee() {
        ParkingFee fee = new ParkingFee(0.0);

        assertEquals(0.0, fee.getAmountAsDouble(), 0.001);
    }

    @Test
    void shouldCompareFeesCorrectly() {
        ParkingFee fee1 = new ParkingFee(10.00);
        ParkingFee fee2 = new ParkingFee(15.00);
        ParkingFee fee3 = new ParkingFee(10.00);

        assertTrue(fee1.isLessThan(fee2));
        assertFalse(fee2.isLessThan(fee1));
        assertFalse(fee1.isLessThan(fee3));
    }

    @Test
    void shouldFormatToStringCorrectly() {
        ParkingFee fee = new ParkingFee(15.50);

        assertEquals("$15.50", fee.toString());
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        ParkingFee fee1 = new ParkingFee(15.00);
        ParkingFee fee2 = new ParkingFee(15.00);
        ParkingFee fee3 = new ParkingFee(20.00);

        assertEquals(fee1, fee2);
        assertNotEquals(fee1, fee3);
    }
}

