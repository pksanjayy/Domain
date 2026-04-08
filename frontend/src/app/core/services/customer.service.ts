import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BaseApiService } from './base-api.service';
import { ApiResponse } from '../models';

export interface CustomerDto {
  id: number;
  name: string;
  mobile: string;
  email?: string;
  branchId: number;
  branchName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService extends BaseApiService {
  private readonly customerUrl = '/api/sales/customers';

  constructor(http: HttpClient) {
    super(http);
  }

  getAllCustomers(): Observable<CustomerDto[]> {
    return this.http.post<ApiResponse<any>>(`${this.customerUrl}/filter`, {
      filters: [],
      sorts: [{ field: 'name', direction: 'ASC' }],
      page: 0,
      size: 1000
    }).pipe(
      map(response => response.data.content)
    );
  }
}
