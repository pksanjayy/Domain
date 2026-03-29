import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ServiceApiService } from '../../services/service-api.service';
import { ServiceBooking } from '../../models/service-booking.model';

@Component({
  selector: 'app-service-booking-detail',
  templateUrl: './service-booking-detail.component.html',
  styleUrls: ['./service-booking-detail.component.scss']
})
export class ServiceBookingDetailComponent implements OnInit {
  booking: ServiceBooking | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private serviceApi: ServiceApiService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadBooking(id);
    }
  }

  private loadBooking(id: number): void {
    this.isLoading = true;
    this.serviceApi.getBooking(id).subscribe({
      next: (data) => {
        this.booking = data.data || data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load booking details', 'Close', { duration: 3000 });
        this.router.navigate(['/service/bookings']);
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'status-confirmed',
      CANCELLED: 'status-cancelled',
      RESCHEDULED: 'status-rescheduled',
      COMPLETED: 'status-completed',
      NO_SHOW: 'status-noshow'
    };
    return map[status] || '';
  }

  editBooking(): void {
    if (this.booking) {
      this.router.navigate(['/service/bookings', this.booking.id, 'edit']);
    }
  }

  deleteBooking(): void {
    if (this.booking && confirm('Are you sure you want to delete this booking?')) {
      this.serviceApi.deleteBooking(this.booking.id!).subscribe({
        next: () => {
          this.snackBar.open('Booking deleted successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/service/bookings']);
        },
        error: () => {
          this.snackBar.open('Error deleting booking', 'Close', { duration: 3000 });
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/service/bookings']);
  }
}
