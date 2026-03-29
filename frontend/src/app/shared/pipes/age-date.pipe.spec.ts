import { AgeDatePipe } from './age-date.pipe';

describe('AgeDatePipe', () => {
  let pipe: AgeDatePipe;

  beforeEach(() => {
    pipe = new AgeDatePipe();
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2026-03-23T12:00:00'));
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('should return "Today" for today\'s date', () => {
    expect(pipe.transform('2026-03-23')).toBe('Today');
  });

  it('should return "1 day" for yesterday', () => {
    expect(pipe.transform('2026-03-22')).toBe('1 day');
  });

  it('should return "5 days" for 5 days ago', () => {
    expect(pipe.transform('2026-03-18')).toBe('5 days');
  });

  it('should return "29 days" for 29 days ago', () => {
    expect(pipe.transform('2026-02-22')).toBe('29 days');
  });

  it('should return "1 month" for ~30 days ago', () => {
    expect(pipe.transform('2026-02-21')).toBe('1 month');
  });

  it('should return "3 months" for ~90 days ago', () => {
    expect(pipe.transform('2025-12-23')).toBe('3 months');
  });

  it('should return "1 year" for ~365 days ago', () => {
    expect(pipe.transform('2025-03-23')).toBe('1 year');
  });

  it('should return "2 years" for ~730 days ago', () => {
    expect(pipe.transform('2024-03-23')).toBe('2 years');
  });

  it('should accept Date objects', () => {
    expect(pipe.transform(new Date('2026-03-22'))).toBe('1 day');
  });

  it('should return empty string for null', () => {
    expect(pipe.transform(null as any)).toBe('');
  });

  it('should return empty string for empty string', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('should return empty string for undefined', () => {
    expect(pipe.transform(undefined as any)).toBe('');
  });
});
