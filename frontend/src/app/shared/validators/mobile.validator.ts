import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Indian mobile number: 10 digits starting with 6-9.
 */
export function mobileValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const mobileRegex = /^[6-9]\d{9}$/;
    return mobileRegex.test(control.value) ? null : { invalidMobile: 'Must be a valid 10-digit Indian mobile number' };
  };
}
