import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';
import { debounceTime, switchMap, map, startWith, catchError } from 'rxjs/operators';
import { InventoryService } from '../../services/inventory.service';
import { VehicleListDto, ArrivalCondition } from '../../models/inventory.model';

@Component({
  selector: 'app-grn-form',
  templateUrl: './grn-form.component.html',
  styleUrls: ['./grn-form.component.scss'],
})
export class GrnFormComponent implements OnInit {
  grnForm!: FormGroup;
  isSubmitting = false;
  isEditMode = false;
  grnId: number | null = null;
  filteredVehicles$!: Observable<VehicleListDto[]>;

  conditions: { value: ArrivalCondition; label: string }[] = [
    { value: 'GOOD', label: 'Good' },
    { value: 'DAMAGED', label: 'Damaged' },
    { value: 'PARTIAL', label: 'Missing Parts' },
  ];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.grnForm = this.fb.group({
      vehicleVin: ['', [Validators.required]],
      arrivalDate: ['', [Validators.required]],
      arrivalCondition: ['', [Validators.required]],
      remarks: ['', [Validators.maxLength(500)]],
    });

    this.filteredVehicles$ = this.grnForm.get('vehicleVin')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      switchMap((value) => {
        if (!value || typeof value !== 'string' || value.length < 3) return of([]);
        return this.inventoryService.searchVehiclesByVin(value).pipe(
          map((res) => {
            if (res.data && res.data.length > 0) {
              this.selectedVehicleId = res.data[0].id; // Store ID for submission
            }
            return res.data;
          }),
          catchError(() => of([]))
        );
      })
    );

    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.grnId = +id;
        this.loadGrnData();
      }
    });
  }

  loadGrnData(): void {
    this.inventoryService.getGrn(this.grnId!).subscribe({
      next: (res) => {
        const grn = res.data;
        this.grnForm.patchValue({
          vehicleVin: grn.vehicleVin,
          arrivalDate: grn.receivedDate,
          arrivalCondition: grn.conditionOnArrival,
          remarks: grn.remarks
        });
        this.selectedVehicleId = grn.vehicleId || 1; // It won't matter on update since API might not even require vehicleId, or we keep original
        this.grnForm.get('vehicleVin')?.disable();
      },
      error: () => {
        this.snackBar.open('Failed to load GRN data', 'Close', { duration: 3000, panelClass: 'error-snackbar' });
      }
    });
  }

  selectedVehicleId: number | null = null;

  onSubmit(): void {
    if (this.grnForm.invalid) {
      this.grnForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.grnForm.value;
    
    // Map to backend CreateGrnRequest structure
    const payload = {
      vehicleId: this.selectedVehicleId, // The Java backend requires vehicleId, not VIN
      receivedDate: formValue.arrivalDate,
      conditionOnArrival: formValue.arrivalCondition,
      remarks: formValue.remarks,
      transporterName: 'Standard Transporter', // Default value
      dispatchDate: formValue.arrivalDate // Default to same day
    };

    if (!payload.vehicleId && !this.isEditMode) {
      this.snackBar.open('Please select a valid vehicle with ID', 'Close', { duration: 3000, panelClass: 'error-snackbar' });
      this.isSubmitting = false;
      return;
    }

    const request$ = this.isEditMode 
      ? this.inventoryService.updateGrn(this.grnId!, payload)
      : this.inventoryService.createGrn(payload as any);

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open(this.isEditMode ? 'GRN updated successfully' : 'GRN created successfully', 'Close', {
          duration: 3000,
          panelClass: 'success-snackbar',
        });
        this.router.navigate(['/inventory/grn']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.snackBar.open(err.error?.message || 'Failed to ' + (this.isEditMode ? 'update' : 'create') + ' GRN', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/inventory/grn']);
  }
}
