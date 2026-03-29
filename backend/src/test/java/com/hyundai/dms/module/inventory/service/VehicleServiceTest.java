package com.hyundai.dms.module.inventory.service;

import com.hyundai.dms.common.filter.SpecificationBuilder;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.module.inventory.dto.VehicleDetailDto;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.enums.FuelType;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.enums.TransmissionType;
import com.hyundai.dms.module.inventory.mapper.VehicleMapper;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.inventory.validator.StockStatusTransitionValidator;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private StockStatusTransitionValidator transitionValidator;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;
    private Branch testBranch;
    private VehicleDetailDto testDetailDto;

    @BeforeEach
    void setUp() {
        testBranch = Branch.builder()
                .code("HYD-01")
                .name("Hyderabad Main")
                .isActive(true)
                .build();
        testBranch.setId(1L);

        testVehicle = Vehicle.builder()
                .vin("MALHC51RXMM000001")
                .brand("Hyundai")
                .model("Creta")
                .variant("SX(O)")
                .colour("Phantom Black")
                .fuelType(FuelType.DIESEL)
                .transmission(TransmissionType.AUTOMATIC)
                .manufacturedDate(java.time.LocalDate.of(2024, 1, 1))
                .msrp(new BigDecimal("1850000"))
                .status(StockStatus.AVAILABLE)
                .branch(testBranch)
                .ageDays(15)
                .build();
        testVehicle.setId(1L);

        testDetailDto = VehicleDetailDto.builder()
                .id(1L)
                .vin("MALHC51RXMM000001")
                .brand("Hyundai")
                .model("Creta")
                .status("HOLD")
                .build();
    }

    @Test
    @DisplayName("holdVehicle transitions AVAILABLE → HOLD successfully")
    void holdVehicle_success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        doNothing().when(transitionValidator).validate(StockStatus.AVAILABLE, StockStatus.HOLD);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        when(vehicleMapper.toDetailDto(any(Vehicle.class))).thenReturn(testDetailDto);

        VehicleDetailDto result = vehicleService.holdVehicle(1L, "Customer interested");

        assertNotNull(result);
        verify(transitionValidator).validate(StockStatus.AVAILABLE, StockStatus.HOLD);
        verify(vehicleRepository).save(testVehicle);
        assertEquals(StockStatus.HOLD, testVehicle.getStatus());
    }

    @Test
    @DisplayName("holdVehicle throws BusinessRuleException on invalid transition")
    void holdVehicle_invalidTransition() {
        testVehicle.setStatus(StockStatus.IN_TRANSIT);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        doThrow(new BusinessRuleException("Invalid status transition from IN_TRANSIT to HOLD"))
                .when(transitionValidator).validate(StockStatus.IN_TRANSIT, StockStatus.HOLD);

        assertThrows(BusinessRuleException.class,
                () -> vehicleService.holdVehicle(1L, "test"));
    }

    @Test
    @DisplayName("holdVehicle handles optimistic lock conflict")
    void holdVehicle_optimisticLockConflict() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        doNothing().when(transitionValidator).validate(StockStatus.AVAILABLE, StockStatus.HOLD);
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Vehicle.class.getName(), 1L));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> vehicleService.holdVehicle(1L, "test"));
        assertTrue(ex.getMessage().contains("concurrently"));
    }
}
