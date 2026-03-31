import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TestDriveService } from '../../services/testdrive.service';
import { TestDriveBooking, TestDriveBookingStatus } from '../../models/testdrive.model';
import { FilterRequest } from '../../../../core/models';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-booking-list',
  template: `
    <div class="page-container">
      <div class="page-header">
        <div class="header-content">
          <h1>Test Drive Bookings</h1>
          <p class="subtitle">Manage customer vehicle test drives.</p>
        </div>
        <div class="header-actions">
          <button mat-raised-button color="primary" (click)="addNew()">
            <mat-icon>add</mat-icon>
            New Booking
          </button>
        </div>
      </div>

      <mat-form-field appearance="outline" class="search-box" style="width: 100%; margin-bottom: 16px;">
        <mat-label>Global Search</mat-label>
        <input matInput #globalSearch (keyup)="applyGlobalSearch(globalSearch.value)" placeholder="Search Booking ID, Customer, Vehicle...">
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <div style="display: flex; gap: 16px; margin-bottom: 16px; flex-wrap: wrap;">
        <mat-form-field appearance="outline" style="min-width: 200px;">
          <mat-label>Status</mat-label>
          <mat-select (selectionChange)="onStatusFilterChange($event.value)">
            <mat-option value="">All Statuses</mat-option>
            <mat-option value="BOOKED">Booked</mat-option>
            <mat-option value="COMPLETED">Completed</mat-option>
            <mat-option value="CANCELLED">Cancelled</mat-option>
            <mat-option value="NO_SHOW">No Show</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" style="min-width: 200px;">
          <mat-label>Pickup Required</mat-label>
          <mat-select (selectionChange)="onPickupFilterChange($event.value)">
            <mat-option value="">All</mat-option>
            <mat-option value="true">Yes</mat-option>
            <mat-option value="false">No</mat-option>
          </mat-select>
        </mat-form-field>
      </div>
      <mat-card class="content-card">
        <div class="table-container">
          <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)">

            <ng-container matColumnDef="bookingId">
              <th mat-header-cell *matHeaderCellDef mat-sort-header> Booking ID </th>
              <td mat-cell *matCellDef="let row"> {{row.bookingId}} </td>
            </ng-container>

            <ng-container matColumnDef="customer">
              <th mat-header-cell *matHeaderCellDef> Customer </th>
              <td mat-cell *matCellDef="let row"> 
                <div>{{row.customerName}}</div>
                <div class="small-text text-muted">{{row.customerMobile}}</div>
              </td>
            </ng-container>

            <ng-container matColumnDef="vehicle">
              <th mat-header-cell *matHeaderCellDef> Vehicle </th>
              <td mat-cell *matCellDef="let row"> 
                <div>{{row.fleetModel}}</div>
                <div class="small-text">{{row.fleetRegistration}}</div>
              </td>
            </ng-container>

            <ng-container matColumnDef="schedule">
              <th mat-header-cell *matHeaderCellDef mat-sort-header="testDriveDate"> Schedule </th>
              <td mat-cell *matCellDef="let row"> 
                <div>{{row.testDriveDate | date}}</div>
                <div class="small-text">{{row.timeSlot}}</div>
              </td>
            </ng-container>

            <ng-container matColumnDef="salesExecutive">
              <th mat-header-cell *matHeaderCellDef> Sales Executive </th>
              <td mat-cell *matCellDef="let row"> {{row.salesExecutiveUsername || 'N/A'}} </td>
            </ng-container>

            <ng-container matColumnDef="licenseNumber">
              <th mat-header-cell *matHeaderCellDef> License No. </th>
              <td mat-cell *matCellDef="let row"> {{row.licenseNumber || 'N/A'}} </td>
            </ng-container>

            <ng-container matColumnDef="pickupRequired">
              <th mat-header-cell *matHeaderCellDef> Pickup </th>
              <td mat-cell *matCellDef="let row"> {{row.pickupRequired ? 'Yes' : 'No'}} </td>
            </ng-container>

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

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef> Actions </th>
              <td mat-cell *matCellDef="let row">
                <div style="display: flex; gap: 8px;">
                  <button mat-icon-button color="primary" (click)="editAction(row)" matTooltip="Edit">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button color="warn" (click)="deleteAction(row)" matTooltip="Cancel Booking">
                    <mat-icon>cancel</mat-icon>
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
    .small-text { font-size: 12px; color: #666; }
  `]
})
export class BookingListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  displayedColumns: string[] = ['bookingId', 'customer', 'vehicle', 'schedule', 'salesExecutive', 'licenseNumber', 'pickupRequired', 'status', 'actions'];
  dataSource = new MatTableDataSource<TestDriveBooking>([]);
  
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  sortField = 'id';
  sortDirection: 'ASC' | 'DESC' = 'DESC';
  globalSearchTerm = '';
  statusFilter = '';
  pickupFilter = '';

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
      // TestDriveBooking entity maps Fleet, which maps Branch.
      filters.push({ field: 'fleet.branch.id', operator: 'EQUAL', value: String(branchId) });
    }
    if (this.globalSearchTerm) {
      filters.push({ field: 'globalSearch', operator: 'LIKE', value: this.globalSearchTerm });
    }
    if (this.statusFilter) {
      filters.push({ field: 'status', operator: 'EQUAL', value: this.statusFilter });
    }
    if (this.pickupFilter) {
      filters.push({ field: 'pickupRequired', operator: 'EQUAL', value: this.pickupFilter });
    }
    const request: FilterRequest = {
      filters: filters,
      sorts: [{ field: this.sortField, direction: this.sortDirection }],
      page: this.pageIndex,
      size: this.pageSize
    };

    this.testDriveService.searchBookings(request).subscribe({
      next: (res) => {
        if (res.data) {
          this.dataSource.data = res.data.content;
          this.totalElements = res.data.totalElements;
        }
      },
      error: (err) => {
        this.snackBar.open('Failed to load booking data', 'Close', { duration: 3000 });
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

  onPickupFilterChange(pickup: string) {
    this.pickupFilter = pickup;
    this.pageIndex = 0;
    this.loadData();
  }

  addNew() {
    this.router.navigate(['/testdrive/bookings/new']);
  }

  editAction(row: TestDriveBooking) {
    this.router.navigate(['/testdrive/bookings', row.id, 'edit']);
  }

  deleteAction(row: TestDriveBooking) {
    if (confirm(`Are you sure you want to cancel booking ${row.bookingId}?`)) {
      this.testDriveService.deleteBooking(row.id).subscribe({
        next: () => {
          this.snackBar.open('Booking cancelled successfully', 'Close', { duration: 3000 });
          this.loadData();
        },
        error: () => this.snackBar.open('Failed to cancel booking', 'Close', { duration: 3000 })
      });
    }
  }

  getStatusColor(status: TestDriveBookingStatus): string {
    switch(status) {
      case TestDriveBookingStatus.BOOKED: return 'primary';
      case TestDriveBookingStatus.COMPLETED: return 'accent';
      case TestDriveBookingStatus.CANCELLED: return 'warn';
      case TestDriveBookingStatus.NO_SHOW: return 'warn';
      default: return '';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
