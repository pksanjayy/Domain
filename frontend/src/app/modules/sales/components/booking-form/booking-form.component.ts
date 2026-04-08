import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SalesService } from '../../services/sales.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { BranchContextService } from '../../../../core/services/branch-context.service';
import { CreateBookingRequest } from '../../models/sales.model';
import { FilterRequest } from '../../../../core/models';

@Component({
  selector: 'app-booking-form',
  templateUrl: './booking-form.component.html',
  styleUrls: ['./booking-form.component.scss']
})
export class BookingFormComponent implements OnInit {
  bookingForm!: FormGroup;
  isLoading = false;
  isEditMode = false;
  bookingId: number | null = null;
  selectedBookingModel: any = null;
  filteredVehicles: any[] = [];
  allVehicles: any[] = [];

  constructor(
    private fb: FormBuilder,
    private salesService: SalesService,
    private inventoryService: InventoryService,
    private branchContext: BranchContextService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAvailableVehicles();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.bookingId = +id;
      this.loadBooking();
    }
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

  loadAvailableVehicles(): void {
    const branchId = this.branchContext.getActiveBranchId();
    const filters: any[] = [
      { field: 'status', operator: 'IN', value: 'AVAILABLE,HOLD' }
    ];
    if (branchId !== null) {
      filters.push({ field: 'branch.id', operator: 'EQUAL', value: String(branchId) });
    }
    const filterRequest: FilterRequest = {
      filters: filters,
      sorts: [],
      page: 0,
      size: 1000,
    };
    this.inventoryService.getVehicles(filterRequest).subscribe({
      next: (res) => {
        this.allVehicles = res.data.content;
      },
      error: () => {
        this.allVehicles = [];
      }
    });
  }

  loadBooking(): void {
    if (!this.bookingId) return;
    
    this.isLoading = true;
    this.salesService.getBooking(this.bookingId).subscribe({
      next: (res) => {
        const booking = res.data;
        
        // Wait for vehicles to load before setting the model
        const checkVehiclesLoaded = setInterval(() => {
          if (this.allVehicles.length > 0) {
            clearInterval(checkVehiclesLoaded);
            
            // Parse vehicleModel string (e.g., "Hyundai Creta") into brand and model
            if (booking.vehicleModel) {
              const parts = booking.vehicleModel.trim().split(/\s+/);
              if (parts.length >= 2) {
                this.selectedBookingModel = {
                  brand: parts[0],
                  model: parts.slice(1).join(' ')
                };
                // Filter vehicles based on the model
                this.filteredVehicles = this.allVehicles.filter(v => 
                  v.brand === parts[0] && v.model === parts.slice(1).join(' ')
                );
              }
            }
            
            this.bookingForm.patchValue({
              leadId: booking.leadId,
              vehicleId: booking.vehicleId,
              totalAmount: booking.totalAmount,
              amountPaid: booking.amountPaid,
              bookingDate: booking.bookingDate ? new Date(booking.bookingDate) : new Date(),
              expectedDelivery: booking.expectedDelivery ? new Date(booking.expectedDelivery) : null
            });
            
            this.isLoading = false;
          }
        }, 100);
        
        // Timeout after 5 seconds
        setTimeout(() => {
          clearInterval(checkVehiclesLoaded);
          if (this.isLoading) {
            this.isLoading = false;
          }
        }, 5000);
      },
      error: () => {
        this.snackBar.open('Failed to load booking', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.onCancel();
      }
    });
  }

  onVehicleModelSelected(model: any): void {
    this.selectedBookingModel = model;
    if (model) {
      this.filteredVehicles = this.allVehicles.filter(v => 
        v.brand === model.brand && v.model === model.model
      );
      if (this.filteredVehicles.length === 0) {
        this.bookingForm.patchValue({ vehicleId: null });
      }
    } else {
      this.filteredVehicles = [];
      this.bookingForm.patchValue({ vehicleId: null });
    }
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
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

    if (this.isEditMode && this.bookingId) {
      this.salesService.updateBooking(this.bookingId, request).subscribe({
        next: () => {
          this.snackBar.open('Booking updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/bookings']);
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to update booking';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    } else {
      this.salesService.createBooking(request).subscribe({
        next: () => {
          this.snackBar.open('Booking created — vehicle is now on HOLD', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/bookings']);
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to create booking';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/sales/bookings']);
  }
}
