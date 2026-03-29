import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * VIN must be exactly 17 alpha-numeric characters (no I, O, Q).
 */
export function vinValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const vinRegex = /^[A-HJ-NPR-Z0-9]{17}$/i;
    return vinRegex.test(control.value) ? null : { invalidVin: 'VIN must be 17 alphanumeric characters (no I, O, Q)' };
  };
}
