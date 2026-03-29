import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-payment-form',
  templateUrl: './payment-form.component.html',
  styleUrls: ['./payment-form.component.scss']
})
export class PaymentFormComponent implements OnInit {
  paymentForm!: FormGroup;
  isEditMode = false;
  paymentId!: number;
  isLoading = false;

  paymentMethods = ['CASH', 'CREDIT_CARD', 'UPI', 'BANK_TRANSFER'];
  statusOptions = ['PENDING', 'PARTIAL', 'PAID'];

  constructor(
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.paymentId = +params['id'];
        this.loadPaymentData();
      }
    });
  }

  initForm(): void {
    this.paymentForm = this.fb.group({
      customerId: [null, Validators.required],
      paymentDate: [new Date(), Validators.required],
      totalPrice: [null, [Validators.required, Validators.min(0.01)]],
      amountPaid: [null, [Validators.required, Validators.min(0.01)]],
      paymentMethod: ['CASH', Validators.required],
      transactionId: [''],
      paymentStatus: ['PENDING', Validators.required]
    });
  }

  loadPaymentData(): void {
    this.isLoading = true;
    this.paymentService.getPayment(this.paymentId).subscribe({
      next: (data) => {
        this.paymentForm.patchValue(data);
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Error loading payment data', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const paymentData = this.paymentForm.value;

    if (this.isEditMode) {
      this.paymentService.updatePayment(this.paymentId, paymentData).subscribe({
        next: () => {
          this.snackBar.open('Payment updated successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/payments']);
        },
        error: () => {
          this.snackBar.open('Error updating payment', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    } else {
      this.paymentService.createPayment(paymentData).subscribe({
        next: () => {
          this.snackBar.open('Payment created successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/payments']);
        },
        error: () => {
          this.snackBar.open('Error creating payment', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/sales/payments']);
  }
}
