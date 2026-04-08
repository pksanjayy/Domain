import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subject, interval } from 'rxjs';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { login } from '../../../core/auth/store/auth.actions';
import { selectAuthIsLoading, selectAuthError } from '../../../core/auth/store/auth.selectors';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm!: FormGroup;
  isLoading$!: Observable<boolean>;
  error$!: Observable<string | null>;
  hidePassword = true;
  usernameFocused = false;
  passwordFocused = false;
  remainingAttempts: number | null = null;
  lockTimeRemaining: number | null = null;
  isAccountLocked = false;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder, private store: Store) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(4)]],
    });

    this.isLoading$ = this.store.select(selectAuthIsLoading);
    this.error$ = this.store.select(selectAuthError);
    
    // Subscribe to errors to extract remaining attempts and lock time
    this.error$.pipe(takeUntil(this.destroy$)).subscribe(error => {
      console.log('Raw error received:', error);
      if (error) {
        this.parseErrorMessage(error);
      }
    });

    // Clear lock state when username changes (different user)
    this.loginForm.get('username')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.remainingAttempts = null;
        this.lockTimeRemaining = null;
        this.isAccountLocked = false;
      });
  }

  parseErrorMessage(error: string): void {
    console.log('Parsing error:', error);
    
    // Reset values
    this.remainingAttempts = null;
    this.lockTimeRemaining = null;
    this.isAccountLocked = false;

    // The error might already be a JSON string from the effects
    try {
      const errorObj = JSON.parse(error);
      console.log('Parsed error object:', errorObj);
      
      // Check if it has the structure we expect
      if (errorObj.remainingAttempts !== undefined) {
        this.remainingAttempts = errorObj.remainingAttempts;
        console.log('Remaining attempts:', this.remainingAttempts);
      }
      if (errorObj.lockTimeRemainingSeconds !== undefined) {
        this.lockTimeRemaining = errorObj.lockTimeRemainingSeconds;
        this.isAccountLocked = true;
        console.log('Lock time remaining:', this.lockTimeRemaining);
        this.startCountdown();
      }
    } catch (e) {
      console.log('Error is not JSON, treating as plain string');
      // If parsing fails, the error is just a plain string message
      // No additional details available
    }
  }

  startCountdown(): void {
    if (this.lockTimeRemaining && this.lockTimeRemaining > 0) {
      interval(1000)
        .pipe(
          takeWhile(() => this.lockTimeRemaining! > 0),
          takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.lockTimeRemaining!--;
          if (this.lockTimeRemaining === 0) {
            this.isAccountLocked = false;
          }
        });
    }
  }

  formatLockTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.isAccountLocked) {
      // Clear previous error state when submitting with new credentials
      this.remainingAttempts = null;
      this.lockTimeRemaining = null;
      this.isAccountLocked = false;
      
      this.store.dispatch(
        login({
          request: {
            username: this.loginForm.value.username,
            password: this.loginForm.value.password,
          },
        })
      );
    } else {
      this.loginForm.markAllAsTouched();
    }
  }

  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field);
    if (control?.hasError('required')) return `${field} is required`;
    if (control?.hasError('minlength')) {
      const len = control.errors?.['minlength'].requiredLength;
      return `Minimum ${len} characters`;
    }
    return '';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
