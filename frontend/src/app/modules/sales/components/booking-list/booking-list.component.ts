import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { SalesService } from '../../services/sales.service';
import { BookingDto, CreateBookingRequest } from '../../models/sales.model';
import { FilterRequest } from '../../../../core/models';
import { BranchContextService } from '../../../../core/services/branch-context.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-booking-list',
  templateUrl: './booking-list.component.html',
  styleUrls: ['./booking-list.component.scss'],
})
export class BookingListComponent implements OnInit {
  displayedColumns = [
    'customerName', 'vehicleVin', 'vehicleModel', 'totalAmount', 'amountPaid',
    'bookingDate', 'expectedDelivery', 'status', 'actions'
  ];
  dataSource = new MatTableDataSource<BookingDto>([]);
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  isLoading = false;
  availableYears: number[] = [];
  private destroy$ = new Subject<void>();

  filterValues: any = {
    globalSearch: '',
    status: '',
    yearFilter: ''
  };

  isDrawerOpen = false;
  bookingForm!: FormGroup;
  isSubmitting = false;
  editingBookingId: number | null = null;

  constructor(
    private salesService: SalesService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBookings();
    this.generateYears();
    this.dataSource.filterPredicate = this.createFilter();
    
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadBookings();
      });
  }

  createFilter(): (data: BookingDto, filter: string) => boolean {
    return (data: BookingDto, filter: string): boolean => {
      const searchTerms = JSON.parse(filter);
      const matchSearch = !!(!searchTerms.globalSearch ||
        data.customerName?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.vehicleVin?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.vehicleModel?.toLowerCase().includes(searchTerms.globalSearch));
      const matchStatus = !!(!searchTerms.status || data.status === searchTerms.status);
      
      let matchYear = true;
      if (searchTerms.yearFilter) {
        const bookedYear = new Date(data.bookingDate).getFullYear();
        matchYear = bookedYear === Number(searchTerms.yearFilter);
      }
      
      return matchSearch && matchStatus && matchYear;
    };
  }

  generateYears(): void {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= 2024; i--) {
      this.availableYears.push(i);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForm(): void {
    this.bookingForm = this.fb.group({
      leadId: [null, Validators.required],
      vehicleId: [null, Validators.required],
      totalAmount: [null, [Validators.required, Validators.min(0.01)]],
      amountPaid: [null, [Validators.required, Validators.min(0)]],
      bookingDate: [new Date(), Validators.required],
      expectedDelivery: [null, Validators.required],
    });
  }

  loadBookings(): void {
    this.isLoading = true;
    const filters: any[] = [];
    const branchId = this.branchContext.getActiveBranchId();
    if (branchId !== null) {
      // Booking entity maps Lead, which maps Branch. Check backend entity graph for mapping if needed
      // Assuming Booking -> lead -> branch -> id or Vehicle -> branch -> id
      // Since booking belongs to a lead, filtering on lead.branch.id usually works.
      filters.push({ field: 'lead.branch.id', operator: 'EQUAL', value: String(branchId) });
    }
    const filterRequest: FilterRequest = {
      filters: filters,
      sorts: [],
      page: this.pageIndex,
      size: this.pageSize,
    };
    this.salesService.getBookings(filterRequest).subscribe({
      next: (res) => {
        this.dataSource.data = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadBookings();
  }

  applyFilter(event: Event): void {
    this.filterValues.globalSearch = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  onStatusFilterChange(status: string): void {
    this.filterValues.status = status;
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  onYearFilterChange(year: string): void {
    this.filterValues.yearFilter = year;
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  openCreateDrawer(): void {
    this.editingBookingId = null;
    this.bookingForm.reset();
    this.bookingForm.patchValue({ bookingDate: new Date() });
    this.isDrawerOpen = true;
  }

  editBooking(booking: BookingDto): void {
    this.editingBookingId = booking.id;
    this.bookingForm.patchValue({
      leadId: booking.leadId,
      vehicleId: booking.vehicleId,
      totalAmount: booking.totalAmount,
      amountPaid: booking.amountPaid,
      bookingDate: booking.bookingDate ? new Date(booking.bookingDate) : new Date(),
      expectedDelivery: booking.expectedDelivery ? new Date(booking.expectedDelivery) : null
    });
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
    this.editingBookingId = null;
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const val = this.bookingForm.value;
    const request: CreateBookingRequest = {
      leadId: val.leadId,
      vehicleId: val.vehicleId,
      totalAmount: val.totalAmount,
      amountPaid: val.amountPaid,
      bookingDate: val.bookingDate instanceof Date 
        ? val.bookingDate.toISOString().slice(0, 10)
        : val.bookingDate,
      expectedDelivery: val.expectedDelivery instanceof Date
        ? val.expectedDelivery.toISOString().slice(0, 10)
        : val.expectedDelivery,
    };

    if (this.editingBookingId) {
      this.salesService.updateBooking(this.editingBookingId, request).subscribe({
        next: () => {
          this.snackBar.open('Booking updated successfully', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadBookings();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    } else {
      this.salesService.createBooking(request).subscribe({
        next: () => {
          this.snackBar.open('Booking created — vehicle is now on HOLD', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadBookings();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    }
  }

  cancelBooking(booking: BookingDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cancel Booking',
        message: `Cancel booking #${booking.id} for "${booking.customerName}"? The vehicle hold will be released.`,
        confirmText: 'Cancel Booking',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.cancelBooking(booking.id).subscribe({
          next: () => {
            this.snackBar.open('Booking cancelled', 'Close', { duration: 3000 });
            this.loadBookings();
          },
        });
      }
    });
  }

  deleteBooking(booking: BookingDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Booking',
        message: `Delete booking #${booking.id} permanently? This action cannot be undone.`,
        confirmText: 'Delete',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.deleteBooking(booking.id).subscribe({
          next: () => {
            this.snackBar.open('Booking deleted', 'Close', { duration: 3000 });
            this.loadBookings();
          },
        });
      }
    });
  }

  getStatusColor(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: '#1976d2',
      CANCELLED: '#c62828',
      DELIVERED: '#2e7d32',
    };
    return map[status] || '#666';
  }

  formatCurrency(value: number): string {
    return '₹' + (value ?? 0).toLocaleString('en-IN');
  }

  onRowClick(booking: BookingDto): void {
    this.router.navigate(['/sales/bookings', booking.id]);
  }
}
