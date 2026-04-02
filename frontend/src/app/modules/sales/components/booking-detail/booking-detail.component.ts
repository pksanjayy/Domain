import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SalesService } from '../../services/sales.service';
import { BookingDto } from '../../models/sales.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-booking-detail',
  templateUrl: './booking-detail.component.html',
  styleUrls: ['./booking-detail.component.scss']
})
export class BookingDetailComponent implements OnInit {
  bookingId!: number;
  booking: BookingDto | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private salesService: SalesService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.bookingId = +id;
      this.loadBooking();
    }
  }

  loadBooking(): void {
    this.isLoading = true;
    this.salesService.getBooking(this.bookingId).subscribe({
      next: (res) => {
        this.booking = res.data;
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load booking details', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.goBack();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/sales/bookings']);
  }

  cancelBooking(): void {
    if (!this.booking) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cancel Booking',
        message: `Cancel booking #${this.booking.id} for "${this.booking.customerName}"? The vehicle hold will be released.`,
        confirmText: 'Cancel Booking',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.cancelBooking(this.booking!.id).subscribe({
          next: () => {
            this.snackBar.open('Booking cancelled', 'Close', { duration: 3000 });
            this.loadBooking();
          },
          error: () => {
            this.snackBar.open('Failed to cancel booking', 'Close', { duration: 5000 });
          }
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
