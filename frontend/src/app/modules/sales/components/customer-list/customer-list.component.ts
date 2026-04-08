import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { SalesService } from '../../services/sales.service';
import { CustomerDto } from '../../models/sales.model';
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

  filterValues: any = {
    globalSearch: '',
    branchFilter: '',
    yearFilter: ''
  };

  constructor(
    private salesService: SalesService,
    private http: HttpClient,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  generateYears(): void {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= 2024; i--) {
      this.availableYears.push(i);
    }
  }

  navigateToCreate(): void {
    this.router.navigate(['/sales/customers/new']);
  }

  navigateToEdit(id: number): void {
    this.router.navigate(['/sales/customers/edit', id]);
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
