package com.hyundai.dms.module.sales.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.DuplicateResourceException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.sales.dto.CreateCustomerRequest;
import com.hyundai.dms.module.sales.dto.CustomerDto;
import com.hyundai.dms.module.sales.dto.UpdateCustomerRequest;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.sales.mapper.CustomerMapper;
import com.hyundai.dms.module.sales.repository.CustomerRepository;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final CustomerMapper customerMapper;
    private final QueryDslPredicateBuilder<Customer> predicateBuilder = new QueryDslPredicateBuilder<>(Customer.class);

    @LogExecution
    @Transactional(readOnly = true)
    public PageResponse<CustomerDto> listCustomers(FilterRequest filterRequest) {
        Predicate predicate = predicateBuilder.build(filterRequest.filters());
        
        com.hyundai.dms.module.sales.entity.QCustomer qCustomer = com.hyundai.dms.module.sales.entity.QCustomer.customer;
        com.querydsl.core.BooleanBuilder finalPredicate = new com.querydsl.core.BooleanBuilder(predicate);
        finalPredicate.and(qCustomer.deleted.isFalse());
        
        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts());
        Page<Customer> page = customerRepository.findAll(finalPredicate, pageRequest);
        Page<CustomerDto> dtoPage = page.map(customerMapper::toDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        Customer customer = findCustomerOrThrow(id);
        return customerMapper.toDto(customer);
    }

    @LogExecution
    @Audited(entity = "Customer", action = ActionType.CREATE)
    @PreAuthorize("hasRole('SALES_CRM_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByMobile(request.getMobile())) {
            throw new DuplicateResourceException("Customer", "mobile", request.getMobile());
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        Customer customer = Customer.builder()
                .name(request.getName())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .dob(request.getDob())
                .location(request.getLocation())
                .branch(branch)
                .build();

        customer = customerRepository.save(customer);
        log.info("Created customer: mobile={}", customer.getMobile());
        return customerMapper.toDto(customer);
    }

    @LogExecution
    @Audited(entity = "Customer", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('SALES_CRM_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public CustomerDto updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setDob(request.getDob());
        customer.setLocation(request.getLocation());
        customer.setBranch(branch);

        customer = customerRepository.save(customer);
        log.info("Updated customer id={}", id);
        return customerMapper.toDto(customer);
    }

    @LogExecution
    @Audited(entity = "Customer", action = ActionType.DELETE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customer.setDeleted(true);
        customerRepository.save(customer);
        log.info("Deleted customer id={}", id);
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
