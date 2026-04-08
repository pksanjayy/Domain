import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { vinValidator } from '../../../../shared/validators/vin.validator';
import { InventoryService } from '../../services/inventory.service';
import { VehicleDetailDto, BranchDto } from '../../models/inventory.model';
import { ApiErrorResponse, FieldError } from '../../../../core/models';

@Component({
  selector: 'app-vehicle-form',
  templateUrl: './vehicle-form.component.html',
  styleUrls: ['./vehicle-form.component.scss'],
})
export class VehicleFormComponent implements OnInit {
  vehicleForm!: FormGroup;
  isEdit = false;
  isLoading = false;
  isSubmitting = false;
  vehicleId: number | null = null;
  branches: BranchDto[] = [];
  useExistingModel = false;
  selectedModel: any = null;

  fuelTypes = [
    { value: 'PETROL', label: 'Petrol' },
    { value: 'DIESEL', label: 'Diesel' },
    { value: 'ELECTRIC', label: 'Electric' },
    { value: 'HYBRID', label: 'Hybrid' },
  ];

  transmissionTypes = [
    { value: 'MANUAL', label: 'Manual' },
    { value: 'AUTOMATIC', label: 'Automatic' },
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBranches();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.vehicleId = Number(id);
      this.loadVehicle(this.vehicleId);
    }
  }

  private initForm(): void {
    this.vehicleForm = this.fb.group({
      vin: ['', [Validators.required, vinValidator()]],
      brand: ['', [Validators.required, Validators.maxLength(50)]],
      model: ['', [Validators.required, Validators.maxLength(100)]],
      variant: ['', [Validators.required, Validators.maxLength(100)]],
      colour: ['', [Validators.required, Validators.maxLength(50)]],
      fuelType: ['', [Validators.required]],
      transmissionType: ['', [Validators.required]],
      msrp: [null, [Validators.required, Validators.min(1)]],
      engineNumber: ['', [Validators.required, Validators.maxLength(30)]],
      chassisNumber: ['', [Validators.required, Validators.maxLength(30)]],
      manufactureDate: ['', [Validators.required]],
      keyNumber: ['', [Validators.maxLength(20)]],
      exteriorColourCode: ['', [Validators.maxLength(20)]],
      interiorColourCode: ['', [Validators.maxLength(20)]],
      branchId: [null, [Validators.required]],
      remarks: ['', [Validators.maxLength(500)]],
    });
  }

  private loadBranches(): void {
    this.inventoryService.getBranches().subscribe({
      next: (response) => {
        this.branches = response.data;
      },
    });
  }

  private loadVehicle(id: number): void {
    this.isLoading = true;
    this.inventoryService.getVehicle(id).subscribe({
      next: (response) => {
        this.patchForm(response.data);
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load vehicle', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
        this.router.navigate(['/inventory/vehicles']);
      },
    });
  }

  private patchForm(vehicle: VehicleDetailDto): void {
    this.vehicleForm.patchValue({
      vin: vehicle.vin,
      brand: vehicle.brand,
      model: vehicle.model,
      variant: vehicle.variant,
      colour: vehicle.colour,
      fuelType: vehicle.fuelType,
      transmissionType: vehicle.transmission,
      msrp: vehicle.msrp,
      engineNumber: vehicle.engineNumber,
      chassisNumber: vehicle.chassisNumber,
      manufactureDate: vehicle.manufacturedDate ? new Date(vehicle.manufacturedDate) : null,
      keyNumber: vehicle.keyNumber,
      exteriorColourCode: vehicle.exteriorColourCode,
      interiorColourCode: vehicle.interiorColourCode,
      branchId: vehicle.branchId,
      remarks: vehicle.remarks,
    });
    
    // Set selected model for the selector
    this.selectedModel = {
      brand: vehicle.brand,
      model: vehicle.model
    };
  }

  onModelSelectionModeChange(): void {
    if (this.useExistingModel) {
      // Clear manual inputs when switching to selector
      this.vehicleForm.patchValue({
        brand: '',
        model: ''
      });
    } else {
      // Clear selector when switching to manual
      this.selectedModel = null;
    }
  }

  onExistingModelSelected(model: any): void {
    if (model && model.brand && model.model) {
      this.vehicleForm.patchValue({
        brand: model.brand,
        model: model.model
      });
    }
  }

  onSubmit(): void {
    if (this.vehicleForm.invalid) {
      this.vehicleForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.vehicleForm.value;

    const payload = {
      vin: formValue.vin,
      brand: formValue.brand,
      model: formValue.model,
      variant: formValue.variant,
      colour: formValue.colour,
      fuelType: formValue.fuelType,
      transmission: formValue.transmissionType,
      manufacturedDate: formValue.manufactureDate ? `${new Date(formValue.manufactureDate).getFullYear()}-${String(new Date(formValue.manufactureDate).getMonth() + 1).padStart(2, '0')}-${String(new Date(formValue.manufactureDate).getDate()).padStart(2, '0')}` : null,
      msrp: formValue.msrp,
      engineNumber: formValue.engineNumber,
      chassisNumber: formValue.chassisNumber,
      keyNumber: formValue.keyNumber,
      exteriorColourCode: formValue.exteriorColourCode,
      interiorColourCode: formValue.interiorColourCode,
      branchId: formValue.branchId
    };

    const request$ = this.isEdit
      ? this.inventoryService.updateVehicle(this.vehicleId!, payload as any)
      : this.inventoryService.createVehicle(payload as any);

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open(
          this.isEdit ? 'Vehicle updated successfully' : 'Vehicle created successfully',
          'Close',
          { duration: 3000, panelClass: 'success-snackbar' }
        );
        this.router.navigate(['/inventory/vehicles']);
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.status === 422 && err.error?.fieldErrors) {
          this.mapServerErrors(err.error.fieldErrors);
        } else {
          this.snackBar.open(
            err.error?.message || 'An error occurred',
            'Close',
            { duration: 3000, panelClass: 'error-snackbar' }
          );
        }
      },
    });
  }

  private mapServerErrors(fieldErrors: FieldError[]): void {
    fieldErrors.forEach((fe) => {
      const control = this.vehicleForm.get(fe.field);
      if (control) {
        control.setErrors({ serverError: fe.message });
      }
    });
  }

  getErrorMessage(field: string): string {
    const control = this.vehicleForm.get(field);
    if (!control || !control.errors) return '';

    if (control.errors['required']) return 'This field is required';
    if (control.errors['invalidVin']) return control.errors['invalidVin'];
    if (control.errors['min']) return 'Must be a positive number';
    if (control.errors['maxlength']) return `Max ${control.errors['maxlength'].requiredLength} characters`;
    if (control.errors['serverError']) return control.errors['serverError'];

    return 'Invalid value';
  }

  goBack(): void {
    this.router.navigate(['/inventory/vehicles']);
  }
}
