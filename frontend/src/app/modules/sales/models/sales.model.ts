// ─── Enums ───
export type LeadStage =
  | 'NEW_LEAD'
  | 'TEST_DRIVE'
  | 'QUOTATION'
  | 'BOOKING'
  | 'DELIVERY_READY'
  | 'DELIVERED'
  | 'LOST';

export type BookingStatus = 'ACTIVE' | 'CANCELLED' | 'DELIVERED';

// ─── Customer DTOs ───
export interface CustomerDto {
  id: number;
  name: string;
  mobile: string;
  email: string;
  dob: string | null;
  location: string | null;
  branchId: number;
  branchName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  name: string;
  mobile: string;
  email: string;
  dob: string | null;
  location: string | null;
  branchId: number;
}

export interface UpdateCustomerRequest {
  name: string;
  email: string;
  dob: string | null;
  location: string | null;
  branchId: number;
}

// ─── Lead DTOs ───
export interface LeadDto {
  id: number;
  customerId: number;
  customerName: string;
  customerMobile: string;
  assignedToId: number;
  assignedToUsername: string;
  modelInterested: string;
  source: string;
  stage: LeadStage;
  lostReason: string | null;
  vehicleId: number | null;
  vehicleVin: string | null;
  branchId: number;
  branchName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLeadRequest {
  customerId: number;
  assignedToId?: number;
  modelInterested: string;
  source: string;
  branchId?: number;
}

export interface StageTransitionRequest {
  newStage: LeadStage;
  lostReason?: string;
}

// ─── Booking DTOs ───
export interface BookingDto {
  id: number;
  leadId: number;
  customerName: string;
  vehicleId: number;
  vehicleVin: string;
  vehicleModel: string;
  totalAmount: number;
  amountPaid: number;
  bookingDate: string;
  expectedDelivery: string;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookingRequest {
  leadId: number;
  vehicleId: number;
  totalAmount: number;
  amountPaid: number;
  bookingDate: string;
  expectedDelivery: string;
}
