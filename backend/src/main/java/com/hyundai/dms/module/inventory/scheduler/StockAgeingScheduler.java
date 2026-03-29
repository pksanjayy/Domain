package com.hyundai.dms.module.inventory.scheduler;

import com.hyundai.dms.common.enums.RoleName;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.notification.dto.NotificationDto;
import com.hyundai.dms.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled task to update stock ageing and alert on aged vehicles.
 * Runs daily at 06:00 AM.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockAgeingScheduler {

    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;

    private static final int AGEING_ALERT_THRESHOLD_DAYS = 60;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void updateStockAgeing() {
        log.info("Starting daily stock ageing update...");

        int updated = vehicleRepository.updateAgeDays();
        log.info("Updated age_days for {} vehicles", updated);

        // Find vehicles crossing the 60-day threshold and notify
        List<Vehicle> agedVehicles = vehicleRepository.findAgedVehicles(AGEING_ALERT_THRESHOLD_DAYS);

        if (!agedVehicles.isEmpty()) {
            log.warn("{} vehicles have crossed {} days in stock", agedVehicles.size(), AGEING_ALERT_THRESHOLD_DAYS);

            String message = String.format(
                    "%d vehicle(s) have been in stock for more than %d days. Please review.",
                    agedVehicles.size(), AGEING_ALERT_THRESHOLD_DAYS
            );

            // Notify WORKSHOP_EXEC about aged stock
            notificationService.sendToRole(RoleName.WORKSHOP_EXEC, NotificationDto.builder()
                    .title("Stock Ageing Alert")
                    .message(message)
                    .module("INVENTORY")
                    .priority("CRITICAL")
                    .deepLink("/inventory/vehicles?ageDays=60")
                    .build(), null);
        }

        log.info("Stock ageing update completed.");
    }
}
