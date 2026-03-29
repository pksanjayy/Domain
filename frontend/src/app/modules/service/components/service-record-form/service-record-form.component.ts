import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceApiService } from '../../services/service-api.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-service-record-form',
  templateUrl: './service-record-form.component.html',
  styleUrls: ['./service-record-form.component.scss']
})
export class ServiceRecordFormComponent implements OnInit {
  recordForm!: FormGroup;
  isEditMode = false;
  recordId!: number;
  isLoading = false;

  statusOptions = ['IN_PROGRESS', 'COMPLETED', 'WAITING_FOR_PARTS'];
  paymentOptions = ['PAID', 'UNPAID', 'PARTIAL'];

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
        this.recordId = +params['id'];
        this.loadRecordData();
      }
    });
  }

  initForm(): void {
    const branchId = this.branchContext.getActiveBranchId() ?? 1;
    this.recordForm = this.fb.group({
      branchId: [branchId, Validators.required],
      serviceBookingId: [null, Validators.required],
      serviceDate: [new Date(), Validators.required],
      odometer: [null, Validators.min(0)],
      workPerformed: [''],
      partsUsed: [''],
      noOfTechnicians: [1, Validators.min(1)],
      technicianHours: [0, Validators.min(0)],
      notes: [''],
      status: ['IN_PROGRESS', Validators.required],
      paymentStatus: ['UNPAID', Validators.required]
    });
  }

  loadRecordData(): void {
    this.isLoading = true;
    this.serviceApi.getRecord(this.recordId).subscribe({
      next: (data) => {
        this.recordForm.patchValue(data);
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Error loading record data', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.recordForm.invalid) {
      this.recordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const recordData = this.recordForm.value;

    if (this.isEditMode) {
      this.serviceApi.updateRecord(this.recordId, recordData).subscribe({
        next: () => {
          this.snackBar.open('Record updated successfully!', 'Close');
          this.router.navigate(['/service/records']);
        },
        error: () => {
          this.snackBar.open('Error updating record', 'Close');
          this.isLoading = false;
        }
      });
    } else {
      this.serviceApi.createRecord(recordData).subscribe({
        next: () => {
          this.snackBar.open('Record created successfully!', 'Close');
          this.router.navigate(['/service/records']);
        },
        error: () => {
          this.snackBar.open('Error creating record', 'Close');
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/service/records']);
  }
}
