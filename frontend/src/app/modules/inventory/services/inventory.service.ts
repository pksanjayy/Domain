import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseApiService } from '../../../core/services/base-api.service';
import { ApiResponse, PageResponse, FilterRequest } from '../../../core/models';
import {
  VehicleListDto,
  VehicleDetailDto,
  CreateVehicleRequest,
  UpdateVehicleRequest,
  DashboardSummaryDto,
  GrnDto,
  CreateGrnRequest,
  PdiChecklistDto,
  UpdatePdiItemRequest,
  StockTransferDto,
  RequestTransferRequest,
  BranchDto,
} from '../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService extends BaseApiService {
  private readonly vehicleUrl = '/api/inventory/vehicles';
  private readonly grnUrl = '/api/inventory/grn';
  private readonly pdiUrl = '/api/inventory/pdi';
  private readonly transferUrl = '/api/inventory/transfers';
  private readonly branchUrl = '/api/admin/branches';

  constructor(http: HttpClient) {
    super(http);
  }

  // ─── Dashboard ───
  getDashboardSummary(): Observable<ApiResponse<DashboardSummaryDto>> {
    return this.get<DashboardSummaryDto>(`${this.vehicleUrl}/dashboard-summary`);
  }

  // ─── Vehicles ───
  getVehicles(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<VehicleListDto>>> {
    const req = JSON.parse(JSON.stringify(filterRequest));
    if (req.filters) {
      req.filters.forEach((f: any) => { 
        if (f.field === 'branchName') f.field = 'branch.name'; 
        if (f.field === 'arrivalDate') f.field = 'createdAt';
      });
    }
    if (req.sorts) {
      req.sorts.forEach((s: any) => { 
        if (s.field === 'branchName') s.field = 'branch.name';
        if (s.field === 'arrivalDate') s.field = 'createdAt'; 
      });
    }
    return this.filter<VehicleListDto>(`${this.vehicleUrl}/filter`, req);
  }

  getVehicle(id: number): Observable<ApiResponse<VehicleDetailDto>> {
    return this.get<VehicleDetailDto>(`${this.vehicleUrl}/${id}`);
  }

  createVehicle(request: CreateVehicleRequest): Observable<ApiResponse<VehicleDetailDto>> {
    return this.post<VehicleDetailDto>(this.vehicleUrl, request);
  }

  updateVehicle(id: number, request: UpdateVehicleRequest): Observable<ApiResponse<VehicleDetailDto>> {
    return this.put<VehicleDetailDto>(`${this.vehicleUrl}/${id}`, request);
  }

  holdVehicle(id: number): Observable<ApiResponse<VehicleDetailDto>> {
    return this.patch<VehicleDetailDto>(`${this.vehicleUrl}/${id}/status`, {
      newStatus: 'HOLD',
      remarks: 'Held by sales executive',
    });
  }

  deleteVehicle(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.vehicleUrl}/${id}`);
  }

  searchVehiclesByVin(vin: string): Observable<ApiResponse<VehicleListDto[]>> {
    return this.get<VehicleListDto[]>(`${this.vehicleUrl}/search`, { vin });
  }

  exportVehicles(): Observable<Blob> {
    return this.http.get(`${this.vehicleUrl}/export`, {
      responseType: 'blob',
    });
  }

  // ─── GRN ───
  getGrnRecords(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<GrnDto>>> {
    return this.filter<GrnDto>(`${this.grnUrl}/filter`, filterRequest);
  }

  getGrn(id: number): Observable<ApiResponse<GrnDto>> {
    return this.get<GrnDto>(`${this.grnUrl}/${id}`);
  }

  createGrn(request: CreateGrnRequest): Observable<ApiResponse<GrnDto>> {
    return this.post<GrnDto>(this.grnUrl, request);
  }

  updateGrn(id: number, request: any): Observable<ApiResponse<GrnDto>> {
    return this.put<GrnDto>(`${this.grnUrl}/${id}`, request);
  }

  deleteGrn(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.grnUrl}/${id}`);
  }

  // ─── PDI ───
  getPdiChecklist(vehicleId: number): Observable<ApiResponse<PdiChecklistDto>> {
    return this.get<PdiChecklistDto>(`${this.pdiUrl}/vehicle/${vehicleId}`);
  }

  updatePdiItem(checklistId: number, itemId: number, request: UpdatePdiItemRequest): Observable<ApiResponse<any>> {
    return this.put<any>(`${this.pdiUrl}/${checklistId}/item/${itemId}`, request);
  }

  completePdi(checklistId: number): Observable<ApiResponse<PdiChecklistDto>> {
    return this.post<PdiChecklistDto>(`${this.pdiUrl}/${checklistId}/complete`, {});
  }

  // ─── Stock Transfers ───
  getTransfers(filterRequest: FilterRequest): Observable<ApiResponse<PageResponse<StockTransferDto>>> {
    return this.filter<StockTransferDto>(`${this.transferUrl}/filter`, filterRequest);
  }

  requestTransfer(request: RequestTransferRequest): Observable<ApiResponse<StockTransferDto>> {
    return this.post<StockTransferDto>(this.transferUrl, request);
  }

  approveTransfer(id: number): Observable<ApiResponse<StockTransferDto>> {
    return this.patch<StockTransferDto>(`${this.transferUrl}/${id}/approve`, {});
  }

  rejectTransfer(id: number, remarks: string): Observable<ApiResponse<StockTransferDto>> {
    return this.patch<StockTransferDto>(`${this.transferUrl}/${id}/reject`, { remarks });
  }

  // ─── Branches ───
  getBranches(): Observable<ApiResponse<BranchDto[]>> {
    return this.get<BranchDto[]>(this.branchUrl);
  }
}
