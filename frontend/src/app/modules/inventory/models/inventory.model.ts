// ─── Enums (type unions) ───
export type StockStatus =
  | 'IN_TRANSIT'
  | 'IN_STOCK'
  | 'PDI_PENDING'
  | 'PDI_PASSED'
  | 'PDI_FAILED'
  | 'AVAILABLE'
  | 'RESERVED'
  | 'SOLD'
  | 'DELIVERED';

export type FuelType = 'PETROL' | 'DIESEL' | 'ELECTRIC' | 'HYBRID' | 'CNG';

export type TransmissionType = 'MANUAL' | 'AUTOMATIC' | 'CVT' | 'DCT' | 'AMT';

export type ArrivalCondition = 'GOOD' | 'DAMAGED' | 'PARTIAL';

export type PdiItemResult = 'PASS' | 'FAIL' | 'NA';

export type PdiOverallStatus = 'PENDING' | 'IN_PROGRESS' | 'PASSED' | 'FAILED';

export type TransferStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';

// ─── Vehicle DTOs ───
export interface VehicleListDto {
  id: number;
  vin: string;
  brand: string;
  model: string;
  variant: string;
  colour: string;
  status: StockStatus;
  fuelType: FuelType;
  transmission: TransmissionType;
  msrp: number;
  createdAt: string;
  ageDays: number;
  branchName: string;
  branchId: number;
}

export interface VehicleAccessoryDto {
  id: number;
  name: string;
  type: string;
  price: number;
}

export interface VehicleDetailDto extends VehicleListDto {
  engineNumber: string;
  chassisNumber: string;
  manufacturedDate: string;
  keyNumber: string;
  exteriorColourCode: string;
  interiorColourCode: string;
  remarks: string;
  accessories: VehicleAccessoryDto[];
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface CreateVehicleRequest {
  vin: string;
  brand: string;
  model: string;
  variant: string;
  colour: string;
  fuelType: FuelType;
  transmissionType: TransmissionType;
  msrp: number;
  engineNumber: string;
  chassisNumber: string;
  manufactureDate: string;
  keyNumber: string;
  exteriorColourCode: string;
  interiorColourCode: string;
  branchId: number;
  remarks: string;
}

export interface UpdateVehicleRequest extends CreateVehicleRequest {}

export interface StatusTransitionRequest {
  newStatus: StockStatus;
  remarks: string;
}

// ─── Dashboard DTOs ───
export interface AgeingBucketDto {
  range: string;
  count: number;
  severity: string;
}

export interface BranchDistributionDto {
  branchName: string;
  count: number;
}

export interface DashboardSummaryDto {
  totalStock: number;
  available: number;
  onHold: number;
  booked: number;
  ageingBuckets: AgeingBucketDto[];
  statusBreakdown: { [key: string]: number };
  branchDistribution: BranchDistributionDto[];
  branchBreakdown: { [branch: string]: { [status: string]: number } };
}

// ─── GRN DTOs ───
export interface GrnDto {
  id: number;
  grnNumber: string;
  vehicleId: number;
  vehicleVin: string;
  transporterName?: string;
  dispatchDate?: string;
  receivedDate: string;
  conditionOnArrival: ArrivalCondition;
  receivedByUsername: string;
  remarks: string;
  createdAt: string;
  createdBy: string;
}

export interface CreateGrnRequest {
  vehicleVin: string;
  arrivalDate: string;
  arrivalCondition: ArrivalCondition;
  remarks: string;
}

// ─── PDI DTOs ───
export interface PdiChecklistItemDto {
  id: number;
  category: string;
  itemName: string;
  description: string;
  result: PdiItemResult | null;
  photoUrl: string | null;
  remarks: string;
  sortOrder: number;
}

export interface PdiChecklistDto {
  id: number;
  vehicleId: number;
  vehicleVin: string;
  overallStatus: PdiOverallStatus;
  completedAt: string | null;
  completedBy: string | null;
  items: PdiChecklistItemDto[];
  createdAt: string;
}

export interface UpdatePdiItemRequest {
  result: PdiItemResult;
  remarks: string;
  photoUrl: string | null;
}

// ─── Stock Transfer DTOs ───
export interface StockTransferDto {
  id: number;
  vehicleId: number;
  vehicleVin: string;
  vehicleModel: string;
  fromBranchId: number;
  fromBranchName: string;
  toBranchId: number;
  toBranchName: string;
  status: TransferStatus;
  requestedBy: string;
  approvedBy: string | null;
  remarks: string;
  requestedAt: string;
  completedAt: string | null;
}

export interface RequestTransferRequest {
  vehicleId: number;
  toBranchId: number;
  remarks: string;
}

// ─── Branch DTO (for dropdowns) ───
export interface BranchDto {
  id: number;
  name: string;
  code: string;
}
