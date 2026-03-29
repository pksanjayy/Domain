package com.hyundai.dms.module.sales.validator;

import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.module.sales.enums.LeadStage;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Enforces the lead stage state machine.
 * Defines valid forward transitions. LOST is reachable from any non-terminal stage.
 */
@Component
public class LeadStageTransitionValidator {

    private static final Map<LeadStage, Set<LeadStage>> ALLOWED_TRANSITIONS = new EnumMap<>(LeadStage.class);

    static {
        ALLOWED_TRANSITIONS.put(LeadStage.NEW_LEAD, Set.of(LeadStage.TEST_DRIVE, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.TEST_DRIVE, Set.of(LeadStage.QUOTATION, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.QUOTATION, Set.of(LeadStage.BOOKING, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.BOOKING, Set.of(LeadStage.DELIVERY_READY, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.DELIVERY_READY, Set.of(LeadStage.DELIVERED, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.DELIVERED, Set.of()); // Terminal
        ALLOWED_TRANSITIONS.put(LeadStage.LOST, Set.of());      // Terminal
    }

    /**
     * Validates that the transition from {@code from} to {@code to} is allowed.
     *
     * @throws BusinessRuleException if the transition is invalid
     */
    public void validate(LeadStage from, LeadStage to) {
        Set<LeadStage> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BusinessRuleException(
                    String.format("Invalid lead stage transition from %s to %s", from, to)
            );
        }
    }

    /**
     * Returns the set of valid target stages from a given stage.
     */
    public Set<LeadStage> getAllowedTransitions(LeadStage from) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
    }
}
