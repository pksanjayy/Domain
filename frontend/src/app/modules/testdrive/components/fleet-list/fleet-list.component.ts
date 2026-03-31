import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TestDriveService } from '../../services/testdrive.service';
import { TestDriveFleet, TestDriveFleetStatus } from '../../models/testdrive.model';
import { FilterRequest } from '../../../../core/models';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-fleet-list',
  template: `
    <div class="page-container">
      <div class="page-header">
        <div class="header-content">
          <h1>Test Drive Fleet</h1>
          <p class="subtitle">Manage test drive vehicles and their statuses.</p>
        </div>
        <div class="header-actions">
          <button mat-raised-button color="primary" (click)="addNew()">
            <mat-icon>add</mat-icon>
            Add Vehicle
          </button>
        </div>
      </div>

      <mat-form-field appearance="outline" class="search-box" style="width: 100%; margin-bottom: 16px;">
        <mat-label>Global Search</mat-label>
        <input matInput #globalSearch (keyup)="applyGlobalSearch(globalSearch.value)" placeholder="Search VIN, Brand, Model...">
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <div style="display: flex; gap: 16px; margin-bottom: 16px; flex-wrap: wrap;">
        <mat-form-field appearance="outline" style="min-width: 200px;">
          <mat-label>Status</mat-label>
          <mat-select (selectionChange)="onStatusFilterChange($event.value)">
            <mat-option value="">All Statuses</mat-option>
            <mat-option value="AVAILABLE">Available</mat-option>
            <mat-option value="ACTIVE">Active</mat-option>
            <mat-option value="BOOKED">Booked</mat-option>
            <mat-option value="MAINTENANCE">Maintenance</mat-option>
            <mat-option value="RETIRED">Retired</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" style="min-width: 200px;">
          <mat-label>Fuel Type</mat-label>
          <mat-select (selectionChange)="onFuelTypeFilterChange($event.value)">
            <mat-option value="">All Fuel Types</mat-option>
            <mat-option value="PETROL">Petrol</mat-option>
            <mat-option value="DIESEL">Diesel</mat-option>
            <mat-option value="ELECTRIC">Electric</mat-option>
            <mat-option value="HYBRID">Hybrid</mat-option>
          </mat-select>
        </mat-form-field>
      </div>
      <mat-card class="content-card">
        <div class="table-container">
          <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)">

            <!-- Fleet ID Column -->
            <ng-container matColumnDef="fleetId">
              <th mat-header-cell *matHeaderCellDef mat-sort-header> Fleet ID </th>
              <td mat-cell *matCellDef="let row"> {{row.fleetId}} </td>
            </ng-container>

            <!-- VIN Column -->
            <ng-container matColumnDef="vin">
              <th mat-header-cell *matHeaderCellDef mat-sort-header> VIN </th>
              <td mat-cell *matCellDef="let row"> {{row.vin}} </td>
            </ng-container>

            <!-- Brand/Model Column -->
            <ng-container matColumnDef="brand">
              <th mat-header-cell *matHeaderCellDef mat-sort-header> Brand/Model </th>
              <td mat-cell *matCellDef="let row"> {{row.brand}} {{row.model}} {{row.variant}} </td>
            </ng-container>

            <!-- Fuel Type Column -->
            <ng-container matColumnDef="fuelType">
              <th mat-header-cell *matHeaderCellDef> Fuel Type </th>
              <td mat-cell *matCellDef="let row"> {{row.fuelType}} </td>
            </ng-container>

            <!-- Transmission Column -->
            <ng-container matColumnDef="transmission">
              <th mat-header-cell *matHeaderCellDef> Transmission </th>
              <td mat-cell *matCellDef="let row"> {{row.transmission}} </td>
            </ng-container>

            <!-- Registration Column -->
            <ng-container matColumnDef="registrationNumber">
              <th mat-header-cell *matHeaderCellDef mat-sort-header> Registration </th>
              <td mat-cell *matCellDef="let row"> {{row.registrationNumber || 'N/A'}} </td>
            </ng-container>

            <!-- Odometer Column -->
            <ng-container matColumnDef="currentOdometer">
              <th mat-header-cell *matHeaderCellDef> Odometer (KM) </th>
              <td mat-cell *matCellDef="let row"> {{row.currentOdometer || 0}} </td>
            </ng-container>

            <!-- Branch Column -->
            <ng-container matColumnDef="branchName">
              <th mat-header-cell *matHeaderCellDef> Branch </th>
              <td mat-cell *matCellDef="let row"> {{row.branchName || 'N/A'}} </td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef> Status </th>
              <td mat-cell *matCellDef="let row">
                <mat-chip-set>
                  <mat-chip [color]="getStatusColor(row.status)" selected>
                    {{row.status}}
                  </mat-chip>
                </mat-chip-set>
              </td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef> Actions </th>
              <td mat-cell *matCellDef="let row">
                <div style="display: flex; gap: 8px;">
                  <button mat-icon-button color="primary" (click)="editAction(row)" matTooltip="Edit">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button color="warn" (click)="deleteAction(row)" matTooltip="Delete">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>

          <mat-paginator 
            [length]="totalElements"
            [pageSize]="pageSize"
            [pageSizeOptions]="[5, 10, 25, 100]"
            (page)="onPageChange($event)">
          </mat-paginator>
        </div>
      </mat-card>
    </div>
  `,
  styles: [`
    .page-container { padding: 24px; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .header-content h1 { margin: 0; font-size: 24px; font-weight: 500; }
    .subtitle { color: #666; margin: 4px 0 0 0; }
    .table-container { overflow-x: auto; }
    table { width: 100%; border-collapse: separate; border-spacing: 0; }
  `]
})
export class FleetListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  displayedColumns: string[] = ['fleetId', 'vin', 'brand', 'fuelType', 'transmission', 'registrationNumber', 'currentOdometer', 'branchName', 'status', 'actions'];
  dataSource = new MatTableDataSource<TestDriveFleet>([]);
  
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  sortField = 'id';
  sortDirection: 'ASC' | 'DESC' = 'DESC';
  globalSearchTerm = '';
  statusFilter = '';
  fuelTypeFilter = '';

  constructor(
    private testDriveService: TestDriveService,
    private router: Router,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {}

  ngOnInit() {
    this.loadData();

    // Reload when branch changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadData());
  }

  applyGlobalSearch(value: string) {
    this.globalSearchTerm = value.trim();
    this.pageIndex = 0;
    this.loadData();
  }

  loadData() {
    const filters: any[] = [];
    const branchId = this.branchContext.getActiveBranchId();
    if (branchId !== null) {
      filters.push({ field: 'branch.id', operator: 'EQUAL', value: String(branchId) });
    }
    if (this.globalSearchTerm) {
      filters.push({ field: 'globalSearch', operator: 'LIKE', value: this.globalSearchTerm });
    }
    if (this.statusFilter) {
      filters.push({ field: 'status', operator: 'EQUAL', value: this.statusFilter });
    }
    if (this.fuelTypeFilter) {
      filters.push({ field: 'fuelType', operator: 'EQUAL', value: this.fuelTypeFilter });
    }
    const request: FilterRequest = {
      filters: filters,
      sorts: [{ field: this.sortField, direction: this.sortDirection }],
      page: this.pageIndex,
      size: this.pageSize
    };

    this.testDriveService.searchFleet(request).subscribe({
      next: (res) => {
        if (res.data) {
          this.dataSource.data = res.data.content;
          this.totalElements = res.data.totalElements;
        }
      },
      error: (err) => {
        this.snackBar.open('Failed to load fleet data', 'Close', { duration: 3000 });
      }
    });
  }

  onPageChange(event: any) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadData();
  }

  onSortChange(event: any) {
    if (!event.active || event.direction === '') {
      this.sortField = 'id';
      this.sortDirection = 'DESC';
    } else {
      this.sortField = event.active;
      this.sortDirection = event.direction.toUpperCase() === 'ASC' ? 'ASC' : 'DESC';
    }
    this.pageIndex = 0;
    this.loadData();
  }

  onStatusFilterChange(status: string) {
    this.statusFilter = status;
    this.pageIndex = 0;
    this.loadData();
  }

  onFuelTypeFilterChange(fuelType: string) {
    this.fuelTypeFilter = fuelType;
    this.pageIndex = 0;
    this.loadData();
  }

  addNew() {
    this.router.navigate(['/testdrive/fleet/new']);
  }

  editAction(row: TestDriveFleet) {
    this.router.navigate(['/testdrive/fleet', row.id, 'edit']);
  }

  deleteAction(row: TestDriveFleet) {
    if (confirm(`Are you sure you want to delete vehicle ${row.vin}?`)) {
      this.testDriveService.deleteFleet(row.id).subscribe({
        next: () => {
          this.snackBar.open('Vehicle deleted successfully', 'Close', { duration: 3000 });
          this.loadData();
        },
        error: () => this.snackBar.open('Failed to delete vehicle', 'Close', { duration: 3000 })
      });
    }
  }

  getStatusColor(status: TestDriveFleetStatus): string {
    switch(status) {
      case TestDriveFleetStatus.AVAILABLE: return 'primary';
      case TestDriveFleetStatus.ACTIVE: return 'primary';
      case TestDriveFleetStatus.BOOKED: return 'accent';
      case TestDriveFleetStatus.MAINTENANCE: return 'warn';
      case TestDriveFleetStatus.RETIRED: return 'warn';
      default: return '';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
