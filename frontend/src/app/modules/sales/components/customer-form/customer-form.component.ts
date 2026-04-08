import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { SalesService } from '../../services/sales.service';
import { BranchContextService } from '../../../../core/services/branch-context.service';
import { CreateCustomerRequest, UpdateCustomerRequest } from '../../models/sales.model';

@Component({
  selector: 'app-customer-form',
  templateUrl: './customer-form.component.html',
  styleUrls: ['./customer-form.component.scss']
})
export class CustomerFormComponent implements OnInit {
  customerForm!: FormGroup;
  isLoading = false;
  isEditMode = false;
  customerId: number | null = null;
  branches: { id: number; name: string }[] = [];

  constructor(
    private fb: FormBuilder,
    private salesService: SalesService,
    private branchContext: BranchContextService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBranches();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.customerId = +id;
      this.loadCustomer();
    }
  }

  initForm(): void {
    this.customerForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      mobile: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
      email: ['', [Validators.required, Validators.email]],
      dob: [null],
      location: [''],
      branchId: [this.branchContext.getActiveBranchId(), Validators.required],
    });
  }

  loadBranches(): void {
    this.http.get<any>('/api/admin/branches/dropdown').subscribe({
      next: (res) => { this.branches = res.data || []; },
      error: () => {},
    });
  }

  loadCustomer(): void {
    if (!this.customerId) return;
    
    this.isLoading = true;
    this.salesService.getCustomer(this.customerId).subscribe({
      next: (res) => {
        const customer = res.data;
        this.customerForm.patchValue(customer);
        this.customerForm.get('mobile')?.disable(); // Mobile is immutable
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open('Failed to load customer', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.onCancel();
      }
    });
  }

  onSubmit(): void {
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    if (this.isEditMode && this.customerId) {
      const request: UpdateCustomerRequest = {
        name: this.customerForm.value.name,
        email: this.customerForm.value.email,
        dob: this.customerForm.value.dob,
        location: this.customerForm.value.location,
        branchId: this.customerForm.value.branchId,
      };
      
      this.salesService.updateCustomer(this.customerId, request).subscribe({
        next: () => {
          this.snackBar.open('Customer updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/customers']);
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to update customer';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    } else {
      const request: CreateCustomerRequest = this.customerForm.getRawValue();
      
      this.salesService.createCustomer(request).subscribe({
        next: () => {
          this.snackBar.open('Customer created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/customers']);
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to create customer';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/sales/customers']);
  }
}
