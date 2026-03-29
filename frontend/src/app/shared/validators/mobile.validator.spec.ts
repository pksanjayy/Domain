import { FormControl } from '@angular/forms';
import { mobileValidator } from './mobile.validator';

describe('Mobile Validator', () => {
  const validate = mobileValidator();

  // --- Valid mobiles ---

  it('should accept a valid mobile starting with 9', () => {
    const control = new FormControl('9876543210');
    expect(validate(control)).toBeNull();
  });

  it('should accept a valid mobile starting with 8', () => {
    const control = new FormControl('8765432109');
    expect(validate(control)).toBeNull();
  });

  it('should accept a valid mobile starting with 7', () => {
    const control = new FormControl('7654321098');
    expect(validate(control)).toBeNull();
  });

  it('should accept a valid mobile starting with 6', () => {
    const control = new FormControl('6543210987');
    expect(validate(control)).toBeNull();
  });

  it('should pass null value (optional field)', () => {
    const control = new FormControl(null);
    expect(validate(control)).toBeNull();
  });

  it('should pass empty string (optional field)', () => {
    const control = new FormControl('');
    expect(validate(control)).toBeNull();
  });

  // --- Invalid mobiles ---

  it('should reject mobile starting with 5', () => {
    const control = new FormControl('5123456789');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });

  it('should reject mobile starting with 0', () => {
    const control = new FormControl('0123456789');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });

  it('should reject mobile with 9 digits', () => {
    const control = new FormControl('987654321');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });

  it('should reject mobile with 11 digits', () => {
    const control = new FormControl('98765432100');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });

  it('should reject mobile with letters', () => {
    const control = new FormControl('98765ABCDE');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });

  it('should reject mobile with spaces', () => {
    const control = new FormControl('987 654 321');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });

  it('should reject mobile with country code prefix', () => {
    const control = new FormControl('+919876543210');
    expect(validate(control)).toEqual({ invalidMobile: jasmine.any(String) });
  });
});
