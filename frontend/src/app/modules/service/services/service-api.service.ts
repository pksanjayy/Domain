import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ServiceApiService {
  private apiUrl = '/api/service';

  constructor(private http: HttpClient) {}

  // Service Bookings
  getBookingsByBranch(branchId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/bookings/branch/${branchId}`);
  }

  getBooking(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/bookings/${id}`);
  }

  createBooking(booking: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/bookings`, booking);
  }

  updateBooking(id: number, booking: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/bookings/${id}`, booking);
  }

  deleteBooking(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/bookings/${id}`);
  }

  // Service Records
  getRecordsByBranch(branchId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/records/branch/${branchId}`);
  }

  getRecord(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/records/${id}`);
  }

  createRecord(record: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/records`, record);
  }

  updateRecord(id: number, record: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/records/${id}`, record);
  }

  deleteRecord(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/records/${id}`);
  }
}
