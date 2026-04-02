-- =============================================
-- DMS Schema v14.0 — Remove PDI statuses from vehicles
-- =============================================

ALTER TABLE vehicles MODIFY COLUMN status ENUM(
    'IN_TRANSIT',
    'GRN_RECEIVED',
    'AVAILABLE',
    'HOLD',
    'BOOKED',
    'INVOICED',
    'TRANSFERRED',
    'DELETED'
) NOT NULL DEFAULT 'IN_TRANSIT';
