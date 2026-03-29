import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';
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
  private destroy$ = new Subject<void>();

  isDrawerOpen = false;
  bookingForm!: FormGroup;
  isSubmitting = false;

  constructor(
    private salesService: SalesService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBookings();
    
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadBookings();
      });
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
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  openCreateDrawer(): void {
    this.bookingForm.reset();
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
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
}
