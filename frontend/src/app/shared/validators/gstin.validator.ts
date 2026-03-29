import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * GSTIN: 15-character alphanumeric format.
 * Format: 2-digit state code + 10-char PAN + 1 entity code + 1 'Z' + 1 check digit
 */
export function gstinValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const gstinRegex = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;
    return gstinRegex.test(control.value.toUpperCase()) ? null : { invalidGstin: 'Must be a valid 15-character GSTIN' };
  };
}
