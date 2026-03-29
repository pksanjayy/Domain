package com.hyundai.dms.module.inventory.validator;

import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Enforces the stock status state machine.
 * Defines valid forward transitions and rejects any backward or illegal moves.
 */
@Component
public class StockStatusTransitionValidator {

    private static final Map<StockStatus, Set<StockStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(StockStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(StockStatus.IN_TRANSIT, Set.of(StockStatus.GRN_RECEIVED));
        ALLOWED_TRANSITIONS.put(StockStatus.GRN_RECEIVED, Set.of(StockStatus.PDI_PENDING));
        ALLOWED_TRANSITIONS.put(StockStatus.PDI_PENDING, Set.of(StockStatus.PDI_DONE));
        ALLOWED_TRANSITIONS.put(StockStatus.PDI_DONE, Set.of(StockStatus.AVAILABLE));
        ALLOWED_TRANSITIONS.put(StockStatus.AVAILABLE, Set.of(StockStatus.HOLD, StockStatus.BOOKED, StockStatus.TRANSFERRED));
        ALLOWED_TRANSITIONS.put(StockStatus.HOLD, Set.of(StockStatus.AVAILABLE, StockStatus.BOOKED));
        ALLOWED_TRANSITIONS.put(StockStatus.BOOKED, Set.of(StockStatus.INVOICED, StockStatus.AVAILABLE));
        ALLOWED_TRANSITIONS.put(StockStatus.INVOICED, Set.of()); // Terminal state
        ALLOWED_TRANSITIONS.put(StockStatus.TRANSFERRED, Set.of()); // Terminal state
    }

    /**
     * Validates that the transition from {@code from} to {@code to} is allowed.
     *
     * @throws BusinessRuleException if the transition is invalid
     */
    public void validate(StockStatus from, StockStatus to) {
        Set<StockStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BusinessRuleException(
                    String.format("Invalid status transition from %s to %s", from, to)
            );
        }
    }

    /**
     * Returns the set of valid target statuses from a given status.
     */
    public Set<StockStatus> getAllowedTransitions(StockStatus from) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
    }
}
