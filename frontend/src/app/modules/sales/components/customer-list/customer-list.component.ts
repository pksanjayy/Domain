import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { SalesService } from '../../services/sales.service';
import { CustomerDto, CreateCustomerRequest, UpdateCustomerRequest } from '../../models/sales.model';
import { FilterRequest } from '../../../../core/models';
import { BranchContextService } from '../../../../core/services/branch-context.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss'],
})
export class CustomerListComponent implements OnInit {
  displayedColumns = ['name', 'mobile', 'email', 'location', 'branchName', 'createdAt', 'actions'];
  dataSource = new MatTableDataSource<CustomerDto>([]);
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  isLoading = false;
  branches: { id: number; name: string }[] = [];
  availableYears: number[] = [];
  private destroy$ = new Subject<void>();

  isDrawerOpen = false;
  isEditMode = false;
  editingCustomer: CustomerDto | null = null;
  customerForm!: FormGroup;
  isSubmitting = false;

  filterValues: any = {
    globalSearch: '',
    branchFilter: '',
    yearFilter: ''
  };

  constructor(
    private salesService: SalesService,
    private fb: FormBuilder,
    private http: HttpClient,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCustomers();
    this.loadBranches();
    this.generateYears();
    this.dataSource.filterPredicate = this.createFilter();
    
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadCustomers();
      });
  }

  createFilter(): (data: CustomerDto, filter: string) => boolean {
    return (data: CustomerDto, filter: string): boolean => {
      const searchTerms = JSON.parse(filter);
      const matchSearch = !!(!searchTerms.globalSearch ||
        data.name?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.mobile?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.email?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.location?.toLowerCase().includes(searchTerms.globalSearch));
      const matchBranch = !!(!searchTerms.branchFilter || data.branchName === searchTerms.branchFilter);
      
      let matchYear = true;
      if (searchTerms.yearFilter) {
        const createdYear = new Date(data.createdAt).getFullYear();
        matchYear = createdYear === Number(searchTerms.yearFilter);
      }
      
      return matchSearch && matchBranch && matchYear;
    };
  }

  generateYears(): void {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= 2024; i--) {
      this.availableYears.push(i);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForm(): void {
    this.customerForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      mobile: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
      email: ['', [Validators.required, Validators.email]],
      dob: [null],
      location: [''],
      branchId: [null, Validators.required],
    });
  }

  loadBranches(): void {
    this.http.get<any>('/api/admin/branches/dropdown').subscribe({
      next: (res) => { this.branches = res.data || []; },
      error: () => {},
    });
  }

  loadCustomers(): void {
    this.isLoading = true;
    const filters: any[] = [];
    const branchId = this.branchContext.getActiveBranchId();
    if (branchId !== null) {
      filters.push({ field: 'branch.id', operator: 'EQUAL', value: String(branchId) });
    }
    const filterRequest: FilterRequest = {
      filters: filters,
      sorts: [],
      page: this.pageIndex,
      size: this.pageSize,
    };
    this.salesService.getCustomers(filterRequest).subscribe({
      next: (res) => {
        this.dataSource.data = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCustomers();
  }

  applyFilter(event: Event): void {
    this.filterValues.globalSearch = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  onBranchFilterChange(branchName: string): void {
    this.filterValues.branchFilter = branchName;
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  onYearFilterChange(year: string): void {
    this.filterValues.yearFilter = year;
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  openCreateDrawer(): void {
    this.isEditMode = false;
    this.editingCustomer = null;
    this.customerForm.reset();
    this.customerForm.get('mobile')?.enable();
    this.isDrawerOpen = true;
  }

  openEditDrawer(customer: CustomerDto): void {
    this.isEditMode = true;
    this.editingCustomer = customer;
    this.customerForm.patchValue(customer);
    this.customerForm.get('mobile')?.disable();
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
  }

  onSubmit(): void {
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    if (this.isEditMode && this.editingCustomer) {
      const request: UpdateCustomerRequest = {
        name: this.customerForm.value.name,
        email: this.customerForm.value.email,
        dob: this.customerForm.value.dob,
        location: this.customerForm.value.location,
        branchId: this.customerForm.value.branchId,
      };
      this.salesService.updateCustomer(this.editingCustomer.id, request).subscribe({
        next: () => {
          this.snackBar.open('Customer updated', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadCustomers();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    } else {
      const request: CreateCustomerRequest = this.customerForm.getRawValue();
      this.salesService.createCustomer(request).subscribe({
        next: () => {
          this.snackBar.open('Customer created', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadCustomers();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    }
  }

  deleteCustomer(customer: CustomerDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Customer',
        message: `Delete "${customer.name}"? This cannot be undone.`,
        confirmText: 'Delete',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.deleteCustomer(customer.id).subscribe({
          next: () => {
            this.snackBar.open('Customer deleted', 'Close', { duration: 3000 });
            this.loadCustomers();
          },
        });
      }
    });
  }
}
