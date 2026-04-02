import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { ColumnDef, FilterField } from '../../../../core/models';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { InventoryService } from '../../services/inventory.service';
import { VehicleListDto } from '../../models/inventory.model';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-vehicle-list',
  templateUrl: './vehicle-list.component.html',
  styleUrls: ['./vehicle-list.component.scss'],
})
export class VehicleListComponent implements OnInit, OnDestroy {
  @ViewChild(DataTableComponent) dataTable!: DataTableComponent;

  private destroy$ = new Subject<void>();
  isMobile = false;
  isExporting = false;
  useVirtualScroll = false;

  columns: ColumnDef[] = [
    { field: 'vin', header: 'VIN', sortable: true },
    { field: 'brand', header: 'Brand', sortable: true },
    { field: 'model', header: 'Model', sortable: true },
    { field: 'variant', header: 'Variant', sortable: true },
    { field: 'colour', header: 'Colour', sortable: false },
    { field: 'status', header: 'Status', pipe: 'stockStatus', sortable: true },
    { field: 'ageDays', header: 'Age', pipe: 'ageDays', sortable: true },
    { field: 'branchName', header: 'Branch', sortable: true }
  ];

  filterConfig: FilterField[] = [
    {
      field: 'status',
      label: 'Status',
      type: 'select',
      options: [
        { value: 'IN_TRANSIT', label: 'In Transit' },
        { value: 'GRN_RECEIVED', label: 'GRN Received' },
        { value: 'AVAILABLE', label: 'Available' },
        { value: 'HOLD', label: 'On Hold' },
        { value: 'BOOKED', label: 'Booked' },
        { value: 'INVOICED', label: 'Invoiced' },
        { value: 'TRANSFERRED', label: 'Transferred' },
      ],
    },
    {
      field: 'branchName',
      label: 'Branch',
      type: 'select',
      options: [],
    },
  ];

  apiUrl = '/api/inventory/vehicles/filter';

  constructor(
    private router: Router,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private breakpointObserver: BreakpointObserver,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.breakpointObserver
      .observe(['(max-width: 768px)'])
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        this.isMobile = result.matches;
      });

    // Load branches for dropdown filter
    this.http.get<any>('/api/admin/branches').subscribe({
      next: (res) => {
        const branchFilter = this.filterConfig.find(f => f.field === 'branchName');
        if (branchFilter && res.data) {
          branchFilter.options = res.data.map((b: any) => ({ value: b.name, label: b.name }));
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onRowClick(row: VehicleListDto): void {
    this.router.navigate(['/inventory/vehicles', row.id]);
  }

  applyGlobalSearch(value: string): void {
    // Overriding the default filter logic to pass the global search string
    // This relies on the backend accepting wildcard filters or we inject an implicit field target.
    // For a generic search, we inject "vin": value inside filter components explicitly.
    const searchFilter = { field: 'globalSearch', operator: 'LIKE' as const, value: value.trim() };
    if (this.dataTable) {
      this.dataTable.onFilterChange([searchFilter]);
    }
  }

  editVehicle(event: Event, vehicle: VehicleListDto): void {
    event.stopPropagation();
    this.router.navigate(['/inventory/vehicles', vehicle.id, 'edit']);
  }

  holdVehicle(event: Event, vehicle: VehicleListDto): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Hold Vehicle',
        message: `Are you sure you want to hold vehicle ${vehicle.vin}?`,
        confirmText: 'Hold',
        color: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.inventoryService.holdVehicle(vehicle.id).subscribe({
          next: () => {
            this.snackBar.open('Vehicle held successfully', 'Close', {
              duration: 3000,
              panelClass: 'success-snackbar',
            });
          },
          error: () => {
            this.snackBar.open('Failed to hold vehicle', 'Close', {
              duration: 3000,
              panelClass: 'error-snackbar',
            });
          },
        });
      }
    });
  }

  exportVehicles(): void {
    this.isExporting = true;
    this.inventoryService.exportVehicles().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'vehicles-export.csv';
        link.click();
        window.URL.revokeObjectURL(url);
        this.isExporting = false;
        this.snackBar.open('Export downloaded', 'Close', {
          duration: 3000,
          panelClass: 'success-snackbar',
        });
      },
      error: () => {
        this.isExporting = false;
        this.snackBar.open('Export failed', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
      },
    });
  }

  toggleViewMode(): void {
    this.useVirtualScroll = !this.useVirtualScroll;
  }

  getStatusClass(status: string): string {
    const classMap: Record<string, string> = {
      IN_TRANSIT: 'status-transit',
      GRN_RECEIVED: 'status-stock',
      AVAILABLE: 'status-available',
      HOLD: 'status-reserved',
      BOOKED: 'status-sold',
      INVOICED: 'status-delivered',
      TRANSFERRED: 'status-transit',
    };
    return classMap[status] || '';
  }

  getAgeClass(ageDays: number): string {
    if (ageDays <= 30) return 'age-green';
    if (ageDays <= 60) return 'age-amber';
    if (ageDays <= 90) return 'age-orange';
    return 'age-red';
  }
}
