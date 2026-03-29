import { FormControl } from '@angular/forms';
import { vinValidator } from './vin.validator';

describe('VIN Validator', () => {
  const validate = vinValidator();

  // --- Valid VINs ---

  it('should accept a valid 17-character uppercase VIN', () => {
    const control = new FormControl('MALAM51BLEM123456');
    expect(validate(control)).toBeNull();
  });

  it('should accept a valid VIN with numbers and letters', () => {
    const control = new FormControl('1HGCM82633A123456');
    expect(validate(control)).toBeNull();
  });

  it('should accept mixed case (regex is case-insensitive)', () => {
    const control = new FormControl('malam51blem123456');
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

  // --- Invalid VINs ---

  it('should reject VIN shorter than 17 characters', () => {
    const control = new FormControl('MALAM51BLEM12345');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });

  it('should reject VIN longer than 17 characters', () => {
    const control = new FormControl('MALAM51BLEM1234567');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });

  it('should reject VIN containing I', () => {
    const control = new FormControl('MALAM51BIEM12345I');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });

  it('should reject VIN containing O', () => {
    const control = new FormControl('MALAM51BOEM12345O');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });

  it('should reject VIN containing Q', () => {
    const control = new FormControl('MALAM51BQEM12345Q');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });

  it('should reject VIN with special characters', () => {
    const control = new FormControl('MALAM51B-EM12345!');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });

  it('should reject VIN with spaces', () => {
    const control = new FormControl('MALAM 51BLEM1234');
    expect(validate(control)).toEqual({ invalidVin: jasmine.any(String) });
  });
});
