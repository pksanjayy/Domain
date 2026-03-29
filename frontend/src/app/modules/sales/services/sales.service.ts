import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseApiService } from '../../../core/services/base-api.service';
import { ApiResponse, PageResponse, FilterRequest } from '../../../core/models';
import {
  CustomerDto,
  CreateCustomerRequest,
  UpdateCustomerRequest,
  LeadDto,
  CreateLeadRequest,
  StageTransitionRequest,
  BookingDto,
  CreateBookingRequest,
} from '../models/sales.model';

@Injectable({ providedIn: 'root' })
export class SalesService extends BaseApiService {
  private readonly customerUrl = '/api/sales/customers';
  private readonly leadUrl = '/api/sales/leads';
  private readonly bookingUrl = '/api/sales/bookings';

  constructor(http: HttpClient) {
    super(http);
  }

  // ─── Customers ───
  getCustomers(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<CustomerDto>>> {
    return this.filter<CustomerDto>(`${this.customerUrl}/filter`, filterRequest);
  }

  getCustomer(id: number): Observable<ApiResponse<CustomerDto>> {
    return this.get<CustomerDto>(`${this.customerUrl}/${id}`);
  }

  createCustomer(request: CreateCustomerRequest): Observable<ApiResponse<CustomerDto>> {
    return this.post<CustomerDto>(this.customerUrl, request);
  }

  updateCustomer(id: number, request: UpdateCustomerRequest): Observable<ApiResponse<CustomerDto>> {
    return this.put<CustomerDto>(`${this.customerUrl}/${id}`, request);
  }

  deleteCustomer(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.customerUrl}/${id}`);
  }

  // ─── Leads ───
  getLeads(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<LeadDto>>> {
    return this.filter<LeadDto>(`${this.leadUrl}/filter`, filterRequest);
  }

  getLead(id: number): Observable<ApiResponse<LeadDto>> {
    return this.get<LeadDto>(`${this.leadUrl}/${id}`);
  }

  createLead(request: CreateLeadRequest): Observable<ApiResponse<LeadDto>> {
    return this.post<LeadDto>(this.leadUrl, request);
  }

  transitionLeadStage(id: number, request: StageTransitionRequest): Observable<ApiResponse<LeadDto>> {
    return this.patch<LeadDto>(`${this.leadUrl}/${id}/stage`, request);
  }

  deleteLead(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.leadUrl}/${id}`);
  }

  // ─── Bookings ───
  getBookings(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<BookingDto>>> {
    return this.filter<BookingDto>(`${this.bookingUrl}/filter`, filterRequest);
  }

  getBooking(id: number): Observable<ApiResponse<BookingDto>> {
    return this.get<BookingDto>(`${this.bookingUrl}/${id}`);
  }

  createBooking(request: CreateBookingRequest): Observable<ApiResponse<BookingDto>> {
    return this.post<BookingDto>(this.bookingUrl, request);
  }

  cancelBooking(id: number): Observable<ApiResponse<BookingDto>> {
    return this.patch<BookingDto>(`${this.bookingUrl}/${id}/cancel`, {});
  }
}

