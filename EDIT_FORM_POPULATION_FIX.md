# Edit Form Population Bug Fix

## Problem
When editing records in leads, bookings, and test drive modules, the customer selector and vehicle model selector dropdowns were not being populated with existing values, even though the data was displayed correctly in the table view.

## Root Cause
1. **Vehicle Model Selector**: The backend returns vehicle model as a string (e.g., "Hyundai Creta"), but the vehicle model selector component expects an object with separate `brand` and `model` properties.

2. **Customer Selector**: The component's `writeValue()` method only checked for customers if they were already loaded, but didn't handle the case where the customer ID was set before the customers list was loaded.

## Solution

### 1. Lead Form (`lead-form.component.ts`)
- Modified `loadLead()` to parse the `modelInterested` string into brand and model components
- Split the string on whitespace, treating the first word as brand and remaining words as model
- Example: "Hyundai Creta" → `{ brand: "Hyundai", model: "Creta" }`

### 2. Booking Form (`booking-form.component.ts`)
- Modified `loadBooking()` to parse the `vehicleModel` string into brand and model components
- After parsing, automatically filters the available vehicles based on the selected model
- This ensures the vehicle dropdown shows only matching vehicles when editing

### 3. Customer Selector Component (`customer-selector.component.ts`)
- Enhanced `writeValue()` to handle asynchronous customer loading
- If customer ID is provided but customers aren't loaded yet, it now loads them first
- Once loaded, it finds and selects the correct customer

### 4. Test Drive Booking Form (`booking-form.component.ts`)
- Replaced the basic customer dropdown with the reusable `CustomerSelectorComponent`
- Removed redundant customer loading logic (now handled by the selector component)
- Removed unused imports (`SalesService`, `CustomerDto`)

## Files Modified
1. `frontend/src/app/modules/sales/components/lead-form/lead-form.component.ts`
2. `frontend/src/app/modules/sales/components/booking-form/booking-form.component.ts`
3. `frontend/src/app/shared/components/customer-selector/customer-selector.component.ts`
4. `frontend/src/app/modules/testdrive/components/booking-form/booking-form.component.ts`

## Testing Checklist
- [ ] Edit a lead - verify customer and model dropdowns are populated
- [ ] Edit a booking - verify customer and vehicle model dropdowns are populated
- [ ] Edit a test drive booking - verify customer dropdown is populated
- [ ] Create new lead - verify dropdowns work correctly
- [ ] Create new booking - verify dropdowns work correctly
- [ ] Create new test drive booking - verify dropdown works correctly

## Notes
- The fleet form doesn't need changes as it uses text inputs for brand and model (not dropdowns)
- All forms now consistently use the `CustomerSelectorComponent` for customer selection
- The parsing logic assumes the model string format is "Brand Model" with space separation
