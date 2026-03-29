package com.hyundai.dms.module.inventory.validator;

import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StockStatusTransitionValidatorTest {

    private StockStatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StockStatusTransitionValidator();
    }

    // ── Valid transitions ──

    @Test
    @DisplayName("IN_TRANSIT → GRN_RECEIVED is valid")
    void inTransitToGrnReceived() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.IN_TRANSIT, StockStatus.GRN_RECEIVED));
    }

    @Test
    @DisplayName("GRN_RECEIVED → PDI_PENDING is valid")
    void grnReceivedToPdiPending() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.GRN_RECEIVED, StockStatus.PDI_PENDING));
    }

    @Test
    @DisplayName("PDI_PENDING → PDI_DONE is valid")
    void pdiPendingToPdiDone() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.PDI_PENDING, StockStatus.PDI_DONE));
    }

    @Test
    @DisplayName("PDI_DONE → AVAILABLE is valid")
    void pdiDoneToAvailable() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.PDI_DONE, StockStatus.AVAILABLE));
    }

    @Test
    @DisplayName("AVAILABLE → HOLD is valid")
    void availableToHold() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.AVAILABLE, StockStatus.HOLD));
    }

    @Test
    @DisplayName("AVAILABLE → BOOKED is valid")
    void availableToBooked() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.AVAILABLE, StockStatus.BOOKED));
    }

    @Test
    @DisplayName("AVAILABLE → TRANSFERRED is valid")
    void availableToTransferred() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.AVAILABLE, StockStatus.TRANSFERRED));
    }

    @Test
    @DisplayName("HOLD → AVAILABLE is valid (release hold)")
    void holdToAvailable() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.HOLD, StockStatus.AVAILABLE));
    }

    @Test
    @DisplayName("HOLD → BOOKED is valid")
    void holdToBooked() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.HOLD, StockStatus.BOOKED));
    }

    @Test
    @DisplayName("BOOKED → INVOICED is valid")
    void bookedToInvoiced() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.BOOKED, StockStatus.INVOICED));
    }

    @Test
    @DisplayName("BOOKED → AVAILABLE is valid (cancel booking)")
    void bookedToAvailable() {
        assertDoesNotThrow(() -> validator.validate(StockStatus.BOOKED, StockStatus.AVAILABLE));
    }

    // ── Invalid (backward) transitions ──

    @Test
    @DisplayName("GRN_RECEIVED → IN_TRANSIT is invalid (backward)")
    void grnReceivedToInTransit_Invalid() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> validator.validate(StockStatus.GRN_RECEIVED, StockStatus.IN_TRANSIT));
        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("AVAILABLE → IN_TRANSIT is invalid")
    void availableToInTransit_Invalid() {
        assertThrows(BusinessRuleException.class,
                () -> validator.validate(StockStatus.AVAILABLE, StockStatus.IN_TRANSIT));
    }

    @Test
    @DisplayName("INVOICED → AVAILABLE is invalid (terminal state)")
    void invoicedToAvailable_Invalid() {
        assertThrows(BusinessRuleException.class,
                () -> validator.validate(StockStatus.INVOICED, StockStatus.AVAILABLE));
    }

    @Test
    @DisplayName("TRANSFERRED → AVAILABLE is invalid (terminal state)")
    void transferredToAvailable_Invalid() {
        assertThrows(BusinessRuleException.class,
                () -> validator.validate(StockStatus.TRANSFERRED, StockStatus.AVAILABLE));
    }

    @Test
    @DisplayName("IN_TRANSIT → AVAILABLE is invalid (skipping steps)")
    void inTransitToAvailable_Invalid() {
        assertThrows(BusinessRuleException.class,
                () -> validator.validate(StockStatus.IN_TRANSIT, StockStatus.AVAILABLE));
    }

    @Test
    @DisplayName("PDI_PENDING → AVAILABLE is invalid (must pass PDI_DONE first)")
    void pdiPendingToAvailable_Invalid() {
        assertThrows(BusinessRuleException.class,
                () -> validator.validate(StockStatus.PDI_PENDING, StockStatus.AVAILABLE));
    }

    // ── getAllowedTransitions ──

    @Test
    @DisplayName("INVOICED has no allowed transitions")
    void invoicedHasNoTransitions() {
        Set<StockStatus> allowed = validator.getAllowedTransitions(StockStatus.INVOICED);
        assertTrue(allowed.isEmpty());
    }

    @Test
    @DisplayName("AVAILABLE has 3 allowed transitions")
    void availableHasThreeTransitions() {
        Set<StockStatus> allowed = validator.getAllowedTransitions(StockStatus.AVAILABLE);
        assertEquals(3, allowed.size());
        assertTrue(allowed.contains(StockStatus.HOLD));
        assertTrue(allowed.contains(StockStatus.BOOKED));
        assertTrue(allowed.contains(StockStatus.TRANSFERRED));
    }
}
