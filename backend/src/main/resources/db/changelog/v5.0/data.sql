-- =============================================
-- DMS Schema v5.0 — Update Menu for Payments
-- =============================================

-- Update Menu ID 9 from Quotations to Payments
UPDATE menus
SET name = 'Payments',
    path = '/sales/payments',
    icon = 'payments',
    updated_by = 'system',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 9;
