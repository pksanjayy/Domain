import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PaymentService } from '../../services/payment.service';
import { Payment } from '../../models/payment.model';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BranchContextService } from '../../../../core/services/branch-context.service';

@Component({
  selector: 'app-payment-list',
  templateUrl: './payment-list.component.html',
  styleUrls: ['./payment-list.component.scss']
})
export class PaymentListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  displayedColumns: string[] = ['paymentId', 'customerName', 'paymentDate', 'totalPrice', 'amountPaid', 'paymentMethod', 'paymentStatus', 'actions'];
  dataSource: MatTableDataSource<Payment>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  isLoading = true;
  filterValues: any = {
    globalSearch: '',
    status: ''
  };

  constructor(
    private paymentService: PaymentService,
    private router: Router,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService
  ) {
    this.dataSource = new MatTableDataSource<Payment>([]);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = this.createFilter();
    this.loadPayments();

    // Reload when branch changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadPayments());
  }

  createFilter(): (data: Payment, filter: string) => boolean {
    return (data: Payment, filter: string): boolean => {
      let searchTerms = JSON.parse(filter);
      
      const matchSearch = data.customerName?.toLowerCase().indexOf(searchTerms.globalSearch) !== -1 ||
                          data.transactionId?.toLowerCase().indexOf(searchTerms.globalSearch) !== -1 ||
                          data.id?.toString().indexOf(searchTerms.globalSearch) !== -1;
                          
      const matchStatus = searchTerms.status ? data.paymentStatus === searchTerms.status : true;
      
      return matchSearch && matchStatus;
    };
  }

  loadPayments(): void {
    const branchId = this.branchContext.getActiveBranchId() ?? 1;
    this.isLoading = true;
    this.paymentService.getAllPaymentsByBranch(branchId).subscribe({
      next: (data) => {
        this.dataSource.data = data;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load payments', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  applyFilter(event: Event) {
    this.filterValues.globalSearch = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = JSON.stringify(this.filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  onStatusFilterChange(status: string) {
    this.filterValues.status = status;
    this.dataSource.filter = JSON.stringify(this.filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  viewPayment(payment: Payment): void {
    this.router.navigate(['/sales/payments', payment.id]);
  }

  editPayment(event: Event, payment: Payment): void {
    event.stopPropagation();
    this.router.navigate(['/sales/payments', payment.id, 'edit']);
  }

  deletePayment(event: Event, payment: Payment): void {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete this payment?`)) {
      this.paymentService.deletePayment(payment.id!).subscribe({
        next: () => {
          this.snackBar.open('Payment deleted successfully', 'Close', { duration: 3000 });
          this.loadPayments();
        },
        error: () => {
          this.snackBar.open('Error deleting payment', 'Close', { duration: 3000 });
        }
      });
    }
  }

  createNew(): void {
    this.router.navigate(['/sales/payments/new']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
