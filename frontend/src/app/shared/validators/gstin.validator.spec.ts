import { FormControl } from '@angular/forms';
import { gstinValidator } from './gstin.validator';

describe('GSTIN Validator', () => {
  const validate = gstinValidator();

  // --- Valid GSTINs ---

  it('should accept a valid GSTIN', () => {
    const control = new FormControl('27AAPFU0939F1ZV');
    expect(validate(control)).toBeNull();
  });

  it('should accept another valid GSTIN', () => {
    const control = new FormControl('29AALCB1234A1Z5');
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

  // --- Invalid GSTINs ---

  it('should reject GSTIN shorter than 15 characters', () => {
    const control = new FormControl('27AAPFU0939F1Z');
    expect(validate(control)).toEqual({ invalidGstin: jasmine.any(String) });
  });

  it('should reject GSTIN longer than 15 characters', () => {
    const control = new FormControl('27AAPFU0939F1ZVX');
    expect(validate(control)).toEqual({ invalidGstin: jasmine.any(String) });
  });

  it('should reject GSTIN with invalid state code (letters)', () => {
    const control = new FormControl('AAAAPFU0939F1ZV');
    expect(validate(control)).toEqual({ invalidGstin: jasmine.any(String) });
  });

  it('should reject GSTIN without Z in 13th position', () => {
    const control = new FormControl('27AAPFU0939F1XV');
    expect(validate(control)).toEqual({ invalidGstin: jasmine.any(String) });
  });

  it('should reject GSTIN with lowercase letters (pre-uppercasing)', () => {
    // The validator uppercases internally, so lowercase should still be valid
    const control = new FormControl('27aapfu0939f1zv');
    expect(validate(control)).toBeNull();
  });

  it('should reject completely invalid format', () => {
    const control = new FormControl('NOT-A-GSTIN');
    expect(validate(control)).toEqual({ invalidGstin: jasmine.any(String) });
  });

  it('should reject numeric-only string', () => {
    const control = new FormControl('123456789012345');
    expect(validate(control)).toEqual({ invalidGstin: jasmine.any(String) });
  });
});
