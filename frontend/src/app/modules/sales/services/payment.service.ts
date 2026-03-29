import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Payment } from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = '/api/sales/payments';

  constructor(private http: HttpClient) {}

  getAllPaymentsByBranch(branchId: number): Observable<Payment[]> {
    return this.http.get<any>(`${this.apiUrl}/branch/${branchId}`).pipe(
      map(response => response.data || response)
    );
  }

  getPayment(id: number): Observable<Payment> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(response => response.data || response)
    );
  }

  createPayment(payment: Payment): Observable<Payment> {
    return this.http.post<any>(this.apiUrl, payment).pipe(
      map(response => response.data || response)
    );
  }

  updatePayment(id: number, payment: Payment): Observable<Payment> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payment).pipe(
      map(response => response.data || response)
    );
  }

  deletePayment(id: number): Observable<void> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`).pipe(
      map(response => response.data || response)
    );
  }
}
