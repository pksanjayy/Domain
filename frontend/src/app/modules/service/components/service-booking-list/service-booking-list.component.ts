import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ServiceApiService } from '../../services/service-api.service';
import { ServiceBooking } from '../../models/service-booking.model';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-service-booking-list',
  templateUrl: './service-booking-list.component.html',
  styleUrls: ['./service-booking-list.component.scss']
})
export class ServiceBookingListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  displayedColumns: string[] = ['bookingId', 'customerName', 'bookingDate', 'serviceType', 'status', 'actions'];
  dataSource: MatTableDataSource<ServiceBooking>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  isLoading = true;
  filterValues: any = {
    globalSearch: '',
    status: '',
    type: ''
  };

  constructor(
    private serviceApi: ServiceApiService,
    private router: Router,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {
    this.dataSource = new MatTableDataSource<ServiceBooking>([]);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = this.createFilter();
    this.loadBookings();

    // Reload when branch changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadBookings());
  }

  createFilter(): (data: ServiceBooking, filter: string) => boolean {
    return (data: ServiceBooking, filter: string): boolean => {
      let searchTerms = JSON.parse(filter);
      const matchSearch = data.customerName?.toLowerCase().indexOf(searchTerms.globalSearch) !== -1 ||
                          data.bookingId?.toLowerCase().indexOf(searchTerms.globalSearch) !== -1;
      const matchStatus = searchTerms.status ? data.status === searchTerms.status : true;
      const matchType = searchTerms.type ? data.serviceType === searchTerms.type : true;
      return matchSearch && matchStatus && matchType;
    };
  }

  loadBookings(): void {
    const branchId = this.branchContext.getActiveBranchId() ?? 1;
    this.isLoading = true;
    this.serviceApi.getBookingsByBranch(branchId).subscribe({
      next: (data) => {
        this.dataSource.data = data;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open('Failed to load service bookings', 'Close', { duration: 3000 });
        this.isLoading = false;
        console.error(err);
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

  onTypeFilterChange(type: string) {
    this.filterValues.type = type;
    this.dataSource.filter = JSON.stringify(this.filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  viewBooking(booking: ServiceBooking): void {
    this.router.navigate(['/service/bookings', booking.id]);
  }

  editBooking(event: Event, booking: ServiceBooking): void {
    event.stopPropagation();
    this.router.navigate(['/service/bookings', booking.id, 'edit']);
  }

  deleteBooking(event: Event, booking: ServiceBooking): void {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete this booking: ${booking.bookingId}?`)) {
      this.serviceApi.deleteBooking(booking.id!).subscribe({
        next: () => {
          this.snackBar.open('Booking deleted successfully', 'Close', { duration: 3000 });
          this.loadBookings();
        },
        error: (err) => {
          this.snackBar.open('Error deleting booking', 'Close', { duration: 3000 });
          console.error(err);
        }
      });
    }
  }

  createNew(): void {
    this.router.navigate(['/service/bookings/new']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
