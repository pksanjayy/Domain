import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TestDriveService } from '../../services/testdrive.service';
import { FuelType, Transmission, TestDriveFleetStatus } from '../../models/testdrive.model';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { BranchDto } from '../../../inventory/models/inventory.model';

@Component({
  selector: 'app-fleet-form',
  template: `
    <div class="page-container">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()" class="back-btn">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div class="header-content">
          <h1>{{ isEditMode ? 'Edit' : 'Add' }} Test Drive Vehicle</h1>
          <p class="subtitle">Enter vehicle details to {{ isEditMode ? 'update' : 'add to' }} the fleet.</p>
        </div>
      </div>

      <mat-card class="form-card">
        <form [formGroup]="fleetForm" (ngSubmit)="onSubmit()">
          
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Fleet ID</mat-label>
              <input matInput formControlName="fleetId" placeholder="e.g. TD-001">
              <mat-error *ngIf="fleetForm.get('fleetId')?.hasError('required')">Fleet ID is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>VIN</mat-label>
              <input matInput formControlName="vin" placeholder="Vehicle Identification Number">
              <mat-error *ngIf="fleetForm.get('vin')?.hasError('required')">VIN is required</mat-error>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Brand</mat-label>
              <input matInput formControlName="brand" placeholder="e.g. Hyundai">
              <mat-error *ngIf="fleetForm.get('brand')?.hasError('required')">Brand is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Model</mat-label>
              <input matInput formControlName="model" placeholder="e.g. Creta">
              <mat-error *ngIf="fleetForm.get('model')?.hasError('required')">Model is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Variant</mat-label>
              <input matInput formControlName="variant" placeholder="e.g. SX Opt">
              <mat-error *ngIf="fleetForm.get('variant')?.hasError('required')">Variant is required</mat-error>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Fuel Type</mat-label>
              <mat-select formControlName="fuelType">
                <mat-option *ngFor="let f of fuelTypes" [value]="f">{{f}}</mat-option>
              </mat-select>
              <mat-error *ngIf="fleetForm.get('fuelType')?.hasError('required')">Fuel Type is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Transmission</mat-label>
              <mat-select formControlName="transmission">
                <mat-option *ngFor="let t of transmissions" [value]="t">{{t}}</mat-option>
              </mat-select>
              <mat-error *ngIf="fleetForm.get('transmission')?.hasError('required')">Transmission is required</mat-error>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Branch</mat-label>
              <mat-select formControlName="branchId">
                <mat-option *ngFor="let b of branches" [value]="b.id">{{b.name}}</mat-option>
              </mat-select>
              <mat-error *ngIf="fleetForm.get('branchId')?.hasError('required')">Branch is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Registration Number</mat-label>
              <input matInput formControlName="registrationNumber" placeholder="e.g. KA-01-AB-1234">
              <mat-error *ngIf="fleetForm.get('registrationNumber')?.hasError('required')">Registration number is required</mat-error>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Status</mat-label>
              <mat-select formControlName="status">
                <mat-option *ngFor="let s of statuses" [value]="s">{{s}}</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Current Odometer (KM)</mat-label>
              <input matInput type="number" formControlName="currentOdometer">
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Insurance Expiry</mat-label>
              <input matInput type="date" formControlName="insuranceExpiry">
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>RC Expiry</mat-label>
              <input matInput type="date" formControlName="rcExpiry">
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Last Service Date</mat-label>
              <input matInput type="date" formControlName="lastServiceDate">
            </mat-form-field>

            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Next Service Due</mat-label>
              <input matInput type="date" formControlName="nextServiceDue">
            </mat-form-field>
          </div>

          <div class="form-actions">
            <button mat-button type="button" (click)="goBack()">Cancel</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="fleetForm.invalid || isSubmitting">
              {{ isSubmitting ? 'Saving...' : 'Save Vehicle' }}
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
    .form-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
  `]
})
export class FleetFormComponent implements OnInit {
  fleetForm: FormGroup;
  isEditMode = false;
  editingId: number | null = null;
  isSubmitting = false;

  fuelTypes = Object.values(FuelType);
  transmissions = Object.values(Transmission);
  statuses = Object.values(TestDriveFleetStatus);
  branches: BranchDto[] = [];

  constructor(
    private fb: FormBuilder,
    private testDriveService: TestDriveService,
    private inventoryService: InventoryService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.fleetForm = this.fb.group({
      fleetId: ['', Validators.required],
      vin: ['', Validators.required],
      brand: ['', Validators.required],
      model: ['', Validators.required],
      variant: ['', Validators.required],
      fuelType: [FuelType.PETROL, Validators.required],
      transmission: [Transmission.MANUAL, Validators.required],
      branchId: [null, Validators.required],
      registrationNumber: ['', Validators.required],
      status: [TestDriveFleetStatus.AVAILABLE, Validators.required],
      currentOdometer: [0, [Validators.required, Validators.min(0)]],
      insuranceExpiry: [null],
      rcExpiry: [null],
      lastServiceDate: [null],
      nextServiceDue: [null]
    });
  }

  ngOnInit() {
    this.loadBranches();
    
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.editingId = +id;
        this.loadFleetData(this.editingId);
      }
    });
  }

  loadBranches() {
    this.inventoryService.getBranches().subscribe({
      next: (res) => {
        if (res.data) this.branches = res.data;
      }
    });
  }

  loadFleetData(id: number) {
    this.testDriveService.getFleetById(id).subscribe({
      next: (res) => {
        if (res.data) {
          this.fleetForm.patchValue(res.data);
        }
      },
      error: () => this.snackBar.open('Failed to load fleet details', 'Close', { duration: 3000 })
    });
  }

  onSubmit() {
    if (this.fleetForm.invalid) return;
    
    this.isSubmitting = true;
    const data = this.fleetForm.value;
    
    if (this.isEditMode && this.editingId) {
      this.testDriveService.updateFleet(this.editingId, data).subscribe({
        next: () => {
          this.snackBar.open('Vehicle updated successfully', 'Close', { duration: 3000 });
          this.goBack();
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Failed to update vehicle', 'Close', { duration: 3000 });
          this.isSubmitting = false;
        }
      });
    } else {
      this.testDriveService.createFleet(data).subscribe({
        next: () => {
          this.snackBar.open('Vehicle created successfully', 'Close', { duration: 3000 });
          this.goBack();
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Failed to create vehicle', 'Close', { duration: 3000 });
          this.isSubmitting = false;
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/testdrive/fleet']);
  }
}
