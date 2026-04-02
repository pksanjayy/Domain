import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TestDriveService } from '../../services/testdrive.service';
import { TestDriveBookingStatus, TestDriveFleet } from '../../models/testdrive.model';
import { SalesService } from '../../../sales/services/sales.service';
import { AdminService } from '../../../admin/services/admin.service';
import { CustomerDto } from '../../../sales/models/sales.model';
import { UserListDto } from '../../../admin/models/admin.model';
import { FilterRequest } from '../../../../core/models';

@Component({
  selector: 'app-booking-form',
// ... template omitted for replace target ... //
  template: `
    <div class="page-container">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()" class="back-btn">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div class="header-content">
          <h1>{{ isEditMode ? 'Edit' : 'New' }} Booking</h1>
          <p class="subtitle">Enter customer and test drive details.</p>
        </div>
      </div>

      <mat-card class="form-card">
        <form [formGroup]="bookingForm" (ngSubmit)="onSubmit()">
          
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Customer</mat-label>
              <mat-select formControlName="customerId">
                <mat-option *ngFor="let c of customers" [value]="c.id">{{c.name}} ({{c.mobile}})</mat-option>
              </mat-select>
              <mat-error *ngIf="bookingForm.get('customerId')?.hasError('required')">Customer is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Fleet Vehicle</mat-label>
              <mat-select formControlName="fleetId">
                <mat-option *ngFor="let f of fleets" [value]="f.id">{{f.model}} - {{f.vin}} ({{f.fleetId}})</mat-option>
              </mat-select>
              <mat-error *ngIf="bookingForm.get('fleetId')?.hasError('required')">Fleet is required</mat-error>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Booking Date</mat-label>
              <input matInput type="date" formControlName="bookingDate">
              <mat-error *ngIf="bookingForm.get('bookingDate')?.hasError('required')">Booking Date is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Test Drive Date</mat-label>
              <input matInput type="date" formControlName="testDriveDate">
              <mat-error *ngIf="bookingForm.get('testDriveDate')?.hasError('required')">Test Drive Date is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Time Slot</mat-label>
              <input matInput type="time" formControlName="timeSlot">
              <mat-error *ngIf="bookingForm.get('timeSlot')?.hasError('required')">Time Slot is required</mat-error>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Sales Executive</mat-label>
              <mat-select formControlName="salesExecutiveId">
                <mat-option *ngFor="let u of salesExecutives" [value]="u.id">{{u.username}}</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>License Number</mat-label>
              <input matInput formControlName="licenseNumber" placeholder="DL...">
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Status</mat-label>
              <mat-select formControlName="status">
                <mat-option *ngFor="let s of statuses" [value]="s">{{s}}</mat-option>
              </mat-select>
            </mat-form-field>

            <div class="checkbox-container">
              <mat-checkbox formControlName="pickupRequired">Pickup Required?</mat-checkbox>
            </div>
          </div>

          <div class="form-actions">
            <button mat-button type="button" (click)="goBack()">Cancel</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="bookingForm.invalid || isSubmitting">
              {{ isSubmitting ? 'Saving...' : 'Save Booking' }}
            </button>
          </div>
        </form>
      </mat-card>
    </div>
  `,
  styles: [`
    .page-container { padding: 24px; max-width: 900px; margin: 0 auto; }
    .page-header { display: flex; align-items: flex-start; margin-bottom: 24px; }
    .back-btn { margin-right: 16px; margin-top: 4px; }
    .header-content h1 { margin: 0; font-size: 24px; font-weight: 500; }
    .subtitle { color: #666; margin: 4px 0 0 0; }
    .form-card { padding: 24px; }
    .form-row { display: flex; gap: 16px; margin-bottom: 8px; }
    .form-field { flex: 1; }
    .checkbox-container { flex: 1; display: flex; align-items: center; padding-left: 16px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
  `]
})
export class BookingFormComponent implements OnInit {
  bookingForm: FormGroup;
  isEditMode = false;
  editingId: number | null = null;
  isSubmitting = false;

  statuses = Object.values(TestDriveBookingStatus);
  customers: CustomerDto[] = [];
  fleets: TestDriveFleet[] = [];
  salesExecutives: UserListDto[] = [];

  constructor(
    private fb: FormBuilder,
    private testDriveService: TestDriveService,
    private salesService: SalesService,
    private adminService: AdminService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    const today = new Date().toISOString().split('T')[0];
    
    this.bookingForm = this.fb.group({
      customerId: [null, Validators.required],
      fleetId: [null, Validators.required],
      bookingDate: [today, Validators.required],
      testDriveDate: [today, Validators.required],
      timeSlot: ['10:00', Validators.required],
      salesExecutiveId: [null],
      licenseNumber: [''],
      pickupRequired: [false],
      status: [TestDriveBookingStatus.BOOKED, Validators.required]
    });
  }

  ngOnInit() {
    this.loadDependencies();

    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.editingId = +id;
        this.loadBookingData(this.editingId);
      }
    });
  }

  loadDependencies() {
    const defaultRequest: FilterRequest = { page: 0, size: 1000, filters: [], sorts: [] };
    this.salesService.getCustomers(defaultRequest).subscribe(res => {
      if (res.data && res.data.content) this.customers = res.data.content;
    });
    this.testDriveService.searchFleet(defaultRequest).subscribe(res => {
      if (res.data && res.data.content) this.fleets = res.data.content;
    });
    this.adminService.getUsers(defaultRequest).subscribe(res => {
      if (res.data && res.data.content) this.salesExecutives = res.data.content;
    });
  }

  loadBookingData(id: number) {
    this.testDriveService.getBookingById(id).subscribe({
      next: (res) => {
        if (res.data) {
          const formValues: any = { ...res.data };
          // Format dates (e.g. 2024-03-24)
          if (formValues.bookingDate) formValues.bookingDate = this.formatDate(formValues.bookingDate);
          if (formValues.testDriveDate) formValues.testDriveDate = this.formatDate(formValues.testDriveDate);
          if (formValues.timeSlot && formValues.timeSlot.includes(':')) {
             formValues.timeSlot = formValues.timeSlot.substring(0, 5); // extract HH:mm
          }

          this.bookingForm.patchValue(formValues);
        }
      },
      error: () => this.snackBar.open('Failed to load booking details', 'Close', { duration: 3000 })
    });
  }

  formatDate(dateVal: any): string {
    if (!dateVal) return '';
    try {
      const d = new Date(dateVal);
      return d.toISOString().split('T')[0];
    } catch {
      return dateVal;
    }
  }

  onSubmit() {
    if (this.bookingForm.invalid) return;
    
    this.isSubmitting = true;
    const data = this.bookingForm.value;
    
    if (data.timeSlot && data.timeSlot.length === 5) {
       data.timeSlot = data.timeSlot + ':00';
    }

    if (this.isEditMode && this.editingId) {
      this.testDriveService.updateBooking(this.editingId, data).subscribe({
        next: () => {
          this.snackBar.open('Booking updated successfully', 'Close', { duration: 3000 });
          this.goBack();
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Failed to update booking', 'Close', { duration: 3000 });
          this.isSubmitting = false;
        }
      });
    } else {
      this.testDriveService.createBooking(data).subscribe({
        next: () => {
          this.snackBar.open('Booking created successfully', 'Close', { duration: 3000 });
          this.goBack();
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Failed to create booking', 'Close', { duration: 3000 });
          this.isSubmitting = false;
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/testdrive/bookings']);
  }
}
