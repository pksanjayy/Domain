package com.hyundai.dms.module.sales.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.inventory.service.VehicleService;
import com.hyundai.dms.module.sales.dto.BookingDto;
import com.hyundai.dms.module.sales.dto.CreateBookingRequest;
import com.hyundai.dms.module.sales.entity.Booking;
import com.hyundai.dms.module.sales.entity.Lead;
import com.hyundai.dms.module.sales.enums.BookingStatus;
import com.hyundai.dms.module.sales.mapper.BookingMapper;
import com.hyundai.dms.module.sales.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.querydsl.core.types.Predicate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final LeadService leadService;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final BookingMapper bookingMapper;
    private final QueryDslPredicateBuilder<Booking> predicateBuilder = new QueryDslPredicateBuilder<>(Booking.class);

    @LogExecution
    @Transactional(readOnly = true)
    public PageResponse<BookingDto> listBookings(FilterRequest filterRequest) {
        Predicate predicate = predicateBuilder.build(filterRequest.filters());
        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts());
        Page<Booking> page = bookingRepository.findAll(predicate, pageRequest);
        Page<BookingDto> dtoPage = page.map(bookingMapper::toDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id) {
        Booking booking = findBookingOrThrow(id);
        return bookingMapper.toDto(booking);
    }

    @LogExecution
    @Audited(entity = "Booking", action = ActionType.CREATE)
    @PreAuthorize("hasRole('ROLE_SALES_CRM_EXEC') or hasRole('ROLE_SUPER_ADMIN')")
    @Transactional
    public BookingDto createBooking(CreateBookingRequest request) {
        if (bookingRepository.existsByLeadId(request.getLeadId())) {
            throw new BusinessRuleException("A booking already exists for lead id=" + request.getLeadId());
        }

        Lead lead = leadService.findLeadOrThrow(request.getLeadId());
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));

        // Hold the vehicle via VehicleService (validates transition + optimistic lock)
        vehicleService.holdVehicle(vehicle.getId(), "Booked for lead #" + lead.getId());

        Booking booking = Booking.builder()
                .lead(lead)
                .vehicle(vehicle)
                .totalAmount(request.getTotalAmount())
                .amountPaid(request.getAmountPaid())
                .bookingDate(request.getBookingDate())
                .expectedDelivery(request.getExpectedDelivery())
                .status(BookingStatus.ACTIVE)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Created booking id={} for lead={}, vehicle VIN={}", booking.getId(),
                lead.getId(), vehicle.getVin());
        return bookingMapper.toDto(booking);
    }

    @LogExecution
    @Audited(entity = "Booking", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('ROLE_SALES_CRM_EXEC') or hasRole('ROLE_SUPER_ADMIN')")
    @Transactional
    public BookingDto cancelBooking(Long id) {
        Booking booking = findBookingOrThrow(id);

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        log.info("Cancelled booking id={}", id);
        return bookingMapper.toDto(booking);
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }
}
