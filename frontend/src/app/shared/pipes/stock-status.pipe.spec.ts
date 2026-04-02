import { StockStatusPipe } from './stock-status.pipe';

describe('StockStatusPipe', () => {
  let pipe: StockStatusPipe;

  beforeEach(() => {
    pipe = new StockStatusPipe();
  });

  // --- All known status mappings ---

  it('should transform IN_TRANSIT to "In Transit"', () => {
    expect(pipe.transform('IN_TRANSIT')).toBe('In Transit');
  });

  it('should transform IN_STOCK to "In Stock"', () => {
    expect(pipe.transform('IN_STOCK')).toBe('In Stock');
  });

  it('should transform AVAILABLE to "Available"', () => {
    expect(pipe.transform('AVAILABLE')).toBe('Available');
  });

  it('should transform RESERVED to "Reserved"', () => {
    expect(pipe.transform('RESERVED')).toBe('Reserved');
  });

  it('should transform SOLD to "Sold"', () => {
    expect(pipe.transform('SOLD')).toBe('Sold');
  });

  it('should transform DELIVERED to "Delivered"', () => {
    expect(pipe.transform('DELIVERED')).toBe('Delivered');
  });

  // --- Fallback / edge cases ---

  it('should replace underscores with spaces for unknown statuses', () => {
    expect(pipe.transform('SOME_NEW_STATUS')).toBe('SOME NEW STATUS');
  });

  it('should return empty string for null', () => {
    expect(pipe.transform(null as any)).toBe('');
  });

  it('should return empty string for undefined', () => {
    expect(pipe.transform(undefined as any)).toBe('');
  });

  it('should return empty string for empty string', () => {
    expect(pipe.transform('')).toBe('');
  });
});
