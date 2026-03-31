import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ColumnDef, FilterField } from '../../../../core/models';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { InventoryService } from '../../services/inventory.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-grn-list',
  templateUrl: './grn-list.component.html',
  styleUrls: ['./grn-list.component.scss'],
})
export class GrnListComponent implements OnInit {
  @ViewChild(DataTableComponent) dataTable!: DataTableComponent;

  columns: ColumnDef[] = [
    { field: 'grnNumber', header: 'GRN Number', sortable: true },
    { field: 'vehicleVin', header: 'Vehicle VIN', sortable: true },
    { field: 'receivedDate', header: 'Arrival Date', sortable: true },
    { field: 'conditionOnArrival', header: 'Condition', sortable: true },
    { field: 'actions', header: 'Actions', sortable: false },
  ];

  filterConfig: FilterField[] = [
    { field: 'grnNumber', label: 'GRN Number', type: 'text' },
    { field: 'vehicleVin', label: 'Vehicle VIN', type: 'text' },
    {
      field: 'conditionOnArrival',
      label: 'Condition',
      type: 'select',
      options: [
        { value: 'GOOD', label: 'Good' },
        { value: 'DAMAGED', label: 'Damaged' },
        { value: 'PARTIAL', label: 'Partial/Missing Parts' },
      ],
    },
    {
      field: 'branchName',
      label: 'Branch',
      type: 'select',
      options: [],
    },
  ];

  apiUrl = '/api/inventory/grn/filter';

  constructor(
    private router: Router,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
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

  onRowClick(row: any): void {
    this.router.navigate(['/inventory/vehicles', row.vehicleId]);
  }

  applyGlobalSearch(value: string): void {
    const searchFilter = { field: 'globalSearch', operator: 'LIKE' as const, value: value.trim() };
    if (this.dataTable) {
      this.dataTable.onFilterChange([searchFilter]);
    }
  }

  createGrn(): void {
    this.router.navigate(['/inventory/grn/new']);
  }

  editGrn(event: Event, row: any): void {
    event.stopPropagation();
    this.router.navigate(['/inventory/grn', row.id, 'edit']);
  }

  deleteGrn(event: Event, row: any): void {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete GRN ${row.grnNumber}?`)) {
      this.inventoryService.deleteGrn(row.id).subscribe({
        next: () => {
          this.snackBar.open('GRN deleted successfully', 'Close', { duration: 3000, panelClass: 'success-snackbar' });
          if (this.dataTable) {
            this.dataTable.loadData();
          }
        },
        error: () => {
          this.snackBar.open('Failed to delete GRN', 'Close', { duration: 3000, panelClass: 'error-snackbar' });
        }
      });
    }
  }
}
