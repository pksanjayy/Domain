package com.hyundai.dms.module.inventory;

import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.validator.StockStatusTransitionValidator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the Inventory flow: Create Vehicle → GRN → PDI → Available.
 * Uses H2 in-memory database (test profile) for fast CI execution.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@org.springframework.test.context.jdbc.Sql(
        scripts = "/test-data.sql",
        executionPhase = org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockStatusTransitionValidator transitionValidator;

    private static Long vehicleId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Create vehicle — status IN_TRANSIT")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void createVehicle() throws Exception {
        String requestBody = """
                {
                    "vin": "MALHC51RXMM000099",
                    "brand": "Hyundai",
                    "model": "Creta",
                    "variant": "SX(O) Turbo",
                    "colour": "Phantom Black",
                    "fuelType": "DIESEL",
                    "transmission": "AUTOMATIC",
                    "manufacturedDate": "2024-01-01",
                    "msrp": 1850000,
                    "branchId": 1
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/inventory/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vin").value("MALHC51RXMM000099"))
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"))
                .andReturn();

        // Extract vehicle ID from response
        String responseBody = result.getResponse().getContentAsString();
        // Simple extraction — in production use Jackson
        int idStart = responseBody.indexOf("\"id\":") + 5;
        int idEnd = responseBody.indexOf(",", idStart);
        vehicleId = Long.parseLong(responseBody.substring(idStart, idEnd).trim());

        assertNotNull(vehicleId);
    }

    @Test
    @Order(2)
    @DisplayName("State machine: verify valid transitions from IN_TRANSIT")
    void verifyInTransitTransitions() {
        // Only GRN_RECEIVED is valid from IN_TRANSIT
        assertDoesNotThrow(() -> transitionValidator.validate(StockStatus.IN_TRANSIT, StockStatus.GRN_RECEIVED));
        assertThrows(Exception.class, () -> transitionValidator.validate(StockStatus.IN_TRANSIT, StockStatus.AVAILABLE));
    }

    @Test
    @Order(3)
    @DisplayName("State machine: full forward path is valid")
    void verifyFullForwardPath() {
        // IN_TRANSIT → GRN_RECEIVED → AVAILABLE → BOOKED → INVOICED
        assertDoesNotThrow(() -> {
            transitionValidator.validate(StockStatus.IN_TRANSIT, StockStatus.GRN_RECEIVED);
            transitionValidator.validate(StockStatus.GRN_RECEIVED, StockStatus.AVAILABLE);
            transitionValidator.validate(StockStatus.AVAILABLE, StockStatus.BOOKED);
            transitionValidator.validate(StockStatus.BOOKED, StockStatus.INVOICED);
        });
    }

    @Test
    @Order(4)
    @DisplayName("Dashboard summary endpoint is accessible")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void dashboardSummary() throws Exception {
        mockMvc.perform(get("/api/inventory/vehicles/dashboard-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStock").exists());
    }
}
