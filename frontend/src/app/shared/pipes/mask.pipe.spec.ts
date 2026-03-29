import { MaskPipe } from './mask.pipe';

describe('MaskPipe', () => {
  let pipe: MaskPipe;

  beforeEach(() => {
    pipe = new MaskPipe();
  });

  it('should mask all but last 4 characters by default', () => {
    expect(pipe.transform('1234567890')).toBe('******7890');
  });

  it('should use custom visible character count', () => {
    expect(pipe.transform('1234567890', 6)).toBe('****567890');
  });

  it('should use custom mask character', () => {
    expect(pipe.transform('1234567890', 4, '#')).toBe('######7890');
  });

  it('should return the value unchanged if it is shorter than visibleChars', () => {
    expect(pipe.transform('abc', 4)).toBe('abc');
  });

  it('should return the value unchanged if it equals visibleChars length', () => {
    expect(pipe.transform('abcd', 4)).toBe('abcd');
  });

  it('should handle null value', () => {
    expect(pipe.transform(null as any)).toBeFalsy();
  });

  it('should handle empty string', () => {
    expect(pipe.transform('')).toBeFalsy();
  });

  it('should handle single character', () => {
    expect(pipe.transform('a', 4)).toBe('a');
  });

  it('should mask with 1 visible character', () => {
    expect(pipe.transform('SECRET', 1)).toBe('*****T');
  });

  it('should mask email-like strings', () => {
    expect(pipe.transform('admin@hyundai.in', 4)).toBe('************i.in');
  });
});
