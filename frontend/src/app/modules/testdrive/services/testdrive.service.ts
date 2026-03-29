import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseApiService } from '../../../core/services/base-api.service';
import { ApiResponse, PageResponse, FilterRequest } from '../../../core/models';
import { TestDriveFleet, TestDriveBooking } from '../models/testdrive.model';

@Injectable({
  providedIn: 'root'
})
export class TestDriveService extends BaseApiService {
  private readonly fleetUrl = '/api/testdrive/fleet';
  private readonly bookingUrl = '/api/testdrive/bookings';

  constructor(http: HttpClient) {
    super(http);
  }

  // Fleet Methods
  searchFleet(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<TestDriveFleet>>> {
    return this.filter<TestDriveFleet>(`${this.fleetUrl}/filter`, filterRequest);
  }

  getFleetById(id: number): Observable<ApiResponse<TestDriveFleet>> {
    return this.get<TestDriveFleet>(`${this.fleetUrl}/${id}`);
  }

  createFleet(data: Partial<TestDriveFleet>): Observable<ApiResponse<TestDriveFleet>> {
    return this.post<TestDriveFleet>(this.fleetUrl, data);
  }

  updateFleet(id: number, data: Partial<TestDriveFleet>): Observable<ApiResponse<TestDriveFleet>> {
    return this.put<TestDriveFleet>(`${this.fleetUrl}/${id}`, data);
  }

  deleteFleet(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.fleetUrl}/${id}`);
  }

  // Booking Methods
  searchBookings(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<TestDriveBooking>>> {
    return this.filter<TestDriveBooking>(`${this.bookingUrl}/filter`, filterRequest);
  }

  getBookingById(id: number): Observable<ApiResponse<TestDriveBooking>> {
    return this.get<TestDriveBooking>(`${this.bookingUrl}/${id}`);
  }

  createBooking(data: Partial<TestDriveBooking>): Observable<ApiResponse<TestDriveBooking>> {
    return this.post<TestDriveBooking>(this.bookingUrl, data);
  }

  updateBooking(id: number, data: Partial<TestDriveBooking>): Observable<ApiResponse<TestDriveBooking>> {
    return this.put<TestDriveBooking>(`${this.bookingUrl}/${id}`, data);
  }

  deleteBooking(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.bookingUrl}/${id}`);
  }
}
