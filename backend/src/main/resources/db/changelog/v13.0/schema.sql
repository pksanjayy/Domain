-- =============================================
-- DMS Schema v13.0 — Add DELETED status to vehicles
-- =============================================

ALTER TABLE vehicles MODIFY COLUMN status ENUM(
    'IN_TRANSIT',
    'GRN_RECEIVED',
    'PDI_PENDING',
    'PDI_DONE',
    'AVAILABLE',
    'HOLD',
    'BOOKED',
    'INVOICED',
    'TRANSFERRED',
    'DELETED'
) NOT NULL DEFAULT 'IN_TRANSIT';
