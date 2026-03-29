import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { BaseApiService } from '../../../core/services/base-api.service';
import { ApiResponse, PageResponse } from '../../../core/models';
import { AuditLogEntry } from '../models/reports.model';

@Injectable({ providedIn: 'root' })
export class ReportsService extends BaseApiService {
  private readonly auditUrl = '/api/admin/audit-logs';
  private readonly vehicleUrl = '/api/inventory/vehicles';

  constructor(http: HttpClient) {
    super(http);
  }

  // Dashboard summary for inventory report
  getInventoryReport(): Observable<ApiResponse<any>> {
    return this.get<any>(`${this.vehicleUrl}/dashboard-summary`);
  }

  // Audit logs
  getAuditLogs(params: {
    from?: string;
    to?: string;
    module?: string;
    action?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<PageResponse<AuditLogEntry>>> {
    return this.get<PageResponse<AuditLogEntry>>(this.auditUrl, params);
  }

  // Sales data - uses bookings API per user request
  getSalesData(year: number): Observable<ApiResponse<PageResponse<any>>> {
    const request = {
      filters: [],
      sorts: [{ field: 'bookingDate', direction: 'DESC' }],
      page: 0,
      size: 1000,
    };
    return this.http.post<ApiResponse<PageResponse<any>>>('/api/sales/bookings/filter', request);
  }
}
