import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PaymentService } from '../../services/payment.service';
import { Payment } from '../../models/payment.model';

@Component({
  selector: 'app-payment-detail',
  templateUrl: './payment-detail.component.html',
  styleUrls: ['./payment-detail.component.scss']
})
export class PaymentDetailComponent implements OnInit {
  payment: Payment | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadPayment(id);
    }
  }

  private loadPayment(id: number): void {
    this.isLoading = true;
    this.paymentService.getPayment(id).subscribe({
      next: (data) => {
        this.payment = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load payment details', 'Close', { duration: 3000 });
        this.router.navigate(['/sales/payments']);
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PAID: 'status-paid',
      PARTIAL: 'status-partial',
      PENDING: 'status-pending'
    };
    return map[status] || '';
  }

  getBalance(): number {
    if (this.payment) {
      return (this.payment.totalPrice || 0) - (this.payment.amountPaid || 0);
    }
    return 0;
  }

  editPayment(): void {
    if (this.payment) {
      this.router.navigate(['/sales/payments', this.payment.id, 'edit']);
    }
  }

  deletePayment(): void {
    if (this.payment && confirm('Are you sure you want to delete this payment?')) {
      this.paymentService.deletePayment(this.payment.id!).subscribe({
        next: () => {
          this.snackBar.open('Payment deleted successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/payments']);
        },
        error: () => {
          this.snackBar.open('Error deleting payment', 'Close', { duration: 3000 });
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/sales/payments']);
  }
}
