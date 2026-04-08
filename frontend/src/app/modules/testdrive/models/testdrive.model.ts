export enum FuelType {
  PETROL = 'PETROL',
  DIESEL = 'DIESEL',
  ELECTRIC = 'ELECTRIC',
  HYBRID = 'HYBRID'
}

export enum Transmission {
  MANUAL = 'MANUAL',
  AUTOMATIC = 'AUTOMATIC'
}

export enum TestDriveFleetStatus {
  AVAILABLE = 'AVAILABLE',
  ACTIVE = 'ACTIVE',
  BOOKED = 'BOOKED',
  MAINTENANCE = 'MAINTENANCE',
  RETIRED = 'RETIRED'
}

export enum TestDriveBookingStatus {
  SCHEDULED = 'SCHEDULED',
  BOOKED = 'BOOKED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW'
}

export interface TestDriveFleet {
  id: number;
  fleetId: string;
  vin: string;
  brand: string;
  model: string;
  variant: string;
  fuelType: FuelType;
  transmission: Transmission;
  registrationNumber: string | null;
  insuranceExpiry: string | null;
  rcExpiry: string | null;
  currentOdometer: number;
  status: TestDriveFleetStatus;
  branchId: number;
  branchName?: string;
  vehicleId?: number;
  vehicleModel?: string;
  lastServiceDate: string | null;
  nextServiceDue: string | null;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface TestDriveBooking {
  id: number;
  bookingId: string;
  customerId: number;
  customerName?: string;
  customerMobile?: string;
  fleetId: number;
  fleetRegistration?: string;
  fleetModel?: string;
  bookingDate: string;
  testDriveDate: string;
  timeSlot: string;
  salesExecutiveId: number | null;
  salesExecutiveUsername?: string;
  licenseNumber: string;
  pickupRequired: boolean;
  status: TestDriveBookingStatus;
  createdAt?: string;
}
