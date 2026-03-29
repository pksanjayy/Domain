import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ServiceApiService } from '../../services/service-api.service';
import { ServiceRecord } from '../../models/service-record.model';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-service-record-list',
  templateUrl: './service-record-list.component.html',
  styleUrls: ['./service-record-list.component.scss']
})
export class ServiceRecordListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  displayedColumns: string[] = ['serviceBookingRef', 'serviceDate', 'odometer', 'status', 'paymentStatus', 'actions'];
  dataSource: MatTableDataSource<ServiceRecord>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  isLoading = true;
  filterValues: any = {
    globalSearch: '',
    status: '',
    paymentStatus: ''
  };

  constructor(
    private serviceApi: ServiceApiService,
    private router: Router,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {
    this.dataSource = new MatTableDataSource<ServiceRecord>([]);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = this.createFilter();
    this.loadRecords();

    // Reload when branch changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadRecords());
  }

  createFilter(): (data: ServiceRecord, filter: string) => boolean {
    return (data: ServiceRecord, filter: string): boolean => {
      let searchTerms = JSON.parse(filter);
      const matchSearch = data.serviceBookingRef?.toLowerCase().indexOf(searchTerms.globalSearch) !== -1;
      const matchStatus = searchTerms.status ? data.status === searchTerms.status : true;
      const matchPayment = searchTerms.paymentStatus ? data.paymentStatus === searchTerms.paymentStatus : true;
      return matchSearch && matchStatus && matchPayment;
    };
  }

  loadRecords(): void {
    const branchId = this.branchContext.getActiveBranchId() ?? 1;
    this.isLoading = true;
    this.serviceApi.getRecordsByBranch(branchId).subscribe({
      next: (data) => {
        this.dataSource.data = data;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open('Failed to load service records', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  applyFilter(event: Event) {
    this.filterValues.globalSearch = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = JSON.stringify(this.filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  onStatusFilterChange(status: string) {
    this.filterValues.status = status;
    this.dataSource.filter = JSON.stringify(this.filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  onPaymentFilterChange(payment: string) {
    this.filterValues.paymentStatus = payment;
    this.dataSource.filter = JSON.stringify(this.filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  viewRecord(record: ServiceRecord): void {
    this.router.navigate(['/service/records', record.id]);
  }

  editRecord(event: Event, record: ServiceRecord): void {
    event.stopPropagation();
    this.router.navigate(['/service/records', record.id, 'edit']);
  }

  deleteRecord(event: Event, record: ServiceRecord): void {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete this record?`)) {
      this.serviceApi.deleteRecord(record.id!).subscribe({
        next: () => {
          this.snackBar.open('Record deleted successfully', 'Close', { duration: 3000 });
          this.loadRecords();
        },
        error: (err) => {
          this.snackBar.open('Error deleting record', 'Close', { duration: 3000 });
        }
      });
    }
  }

  createNew(): void {
    this.router.navigate(['/service/records/new']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
