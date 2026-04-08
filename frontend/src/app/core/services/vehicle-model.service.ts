import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../models';

export interface VehicleModelDto {
  id: number;
  brand: string;
  model: string;
  displayName: string;
  isActive: boolean;
  vehicleCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class VehicleModelService {
  private readonly apiUrl = '/api/inventory/vehicle-models';

  constructor(private http: HttpClient) {}

  getActiveModels(): Observable<VehicleModelDto[]> {
    return this.http.get<ApiResponse<VehicleModelDto[]>>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getAllModels(): Observable<VehicleModelDto[]> {
    return this.http.get<ApiResponse<VehicleModelDto[]>>(`${this.apiUrl}/all`)
      .pipe(map(response => response.data));
  }
}
