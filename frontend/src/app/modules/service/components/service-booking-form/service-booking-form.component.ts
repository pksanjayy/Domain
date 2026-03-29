import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceApiService } from '../../services/service-api.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-service-booking-form',
  templateUrl: './service-booking-form.component.html',
  styleUrls: ['./service-booking-form.component.scss']
})
export class ServiceBookingFormComponent implements OnInit {
  bookingForm!: FormGroup;
  isEditMode = false;
  bookingId!: number;
  isLoading = false;

  serviceTypes = ['FREE', 'PAID', 'REPAIR', 'WARRANTY'];
  statusOptions = ['CONFIRMED', 'CANCELLED', 'RESCHEDULED', 'COMPLETED', 'NO_SHOW'];

  // Data fetching simulating combobox
  customers: any[] = []; // In a real app we'd fetch this from Sales API or have an autocomplete search

  constructor(
    private fb: FormBuilder,
    private serviceApi: ServiceApiService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.bookingId = +params['id'];
        this.loadBookingData();
      }
    });
  }

  initForm(): void {
    const branchId = this.branchContext.getActiveBranchId() ?? 1;
    this.bookingForm = this.fb.group({
      branchId: [branchId, Validators.required],
      customerId: [null, Validators.required],
      bookingDate: [new Date(), Validators.required],
      preferredServiceDate: [null],
      serviceType: ['PAID', Validators.required],
      complaints: [''],
      status: ['CONFIRMED', Validators.required]
    });
  }

  loadBookingData(): void {
    this.isLoading = true;
    this.serviceApi.getBooking(this.bookingId).subscribe({
      next: (data) => {
        this.bookingForm.patchValue(data);
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open('Error loading booking data', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const bookingData = this.bookingForm.value;

    if (this.isEditMode) {
      this.serviceApi.updateBooking(this.bookingId, bookingData).subscribe({
        next: () => {
          this.snackBar.open('Booking updated successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/service/bookings']);
        },
        error: () => {
          this.snackBar.open('Error updating booking', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    } else {
      this.serviceApi.createBooking(bookingData).subscribe({
        next: () => {
          this.snackBar.open('Booking created successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/service/bookings']);
        },
        error: () => {
          this.snackBar.open('Error creating booking', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/service/bookings']);
  }
}
