-- =============================================
-- DMS Data v14.0 — Update vehicles to remove PDI statuses
-- =============================================

UPDATE vehicles SET status = 'AVAILABLE' WHERE status IN ('PDI_PENDING', 'PDI_DONE');
