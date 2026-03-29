export interface SalesReportDto {
  month: string;
  soldCount: number;
  deliveredCount: number;
  totalRevenue: number;
}

export interface InventoryReportDto {
  totalStock: number;
  available: number;
  onHold: number;
  booked: number;
  statusBreakdown: { [key: string]: number };
  branchDistribution: { branchName: string; count: number }[];
  ageingBuckets: { range: string; count: number; severity: string }[];
}

export interface AuditLogEntry {
  id: number;
  entityName: string;
  entityId: number;
  action: string;
  oldValue: string | null;
  newValue: string | null;
  performedBy: number | null;
  performedByUsername: string;
  performedAt: string;
  ipAddress: string;
  correlationId: string;
}
