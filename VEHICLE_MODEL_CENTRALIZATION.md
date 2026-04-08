# Vehicle Model Centralization Implementation

## Overview
Implemented a centralized `VehicleModel` table that stores unique brand+model combinations. All vehicle-related modules now reference this single source of truth for vehicle models.

## Architecture

### Core Concept
```
VehicleModel (Master Table)
    ↓
Vehicle → references VehicleModel
    ↓
├── Leads → references Vehicle → gets model from Vehicle
├── Bookings → references Vehicle → gets model from Vehicle  
├── TestDriveFleet → references Vehicle → gets model from Vehicle
└── ServiceBooking → references Customer (no vehicle)
```

## Database Changes

### New Table: `vehicle_models`
```sql
CREATE TABLE vehicle_models (
    id BIGINT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    vehicle_count INT DEFAULT 0,
    UNIQUE (brand, model)
);
```

**Fields:**
- `brand` + `model`: Unique combination
- `is_active`: FALSE when no vehicles use this model
- `vehicle_count`: Tracks how many vehicles use this model

### Modified Tables

#### `vehicles`
- Added: `vehicle_model_id` (FK to vehicle_models)
- Kept: `brand`, `model` (denormalized for performance)

#### `test_drive_fleet`
- Added: `vehicle_id` (FK to vehicles)
- Removed: `brand`, `model` columns
- Now gets brand/model from linked vehicle

## Backend Implementation

### 1. VehicleModel Entity
**File:** `backend/src/main/java/com/hyundai/dms/module/inventory/entity/VehicleModel.java`

Stores unique vehicle models with usage tracking.

### 2. VehicleModelService
**File:** `backend/src/main/java/com/hyundai/dms/module/inventory/service/VehicleModelService.java`

**Key Methods:**
- `getOrCreateVehicleModel(brand, model)`: Creates or increments count
- `decrementVehicleCount(brand, model)`: Decrements count, deactivates if zero
- `updateVehicleModel(oldBrand, oldModel, newBrand, newModel)`: Handles model changes
- `getAllActiveModels()`: Returns models currently in use

### 3. Updated VehicleService
**File:** `backend/src/main/java/com/hyundai/dms/module/inventory/service/VehicleService.java`

**Changes:**
- `createVehicle()`: Calls `getOrCreateVehicleModel()` before creating vehicle
- `updateVehicle()`: Calls `updateVehicleModel()` if brand/model changed
- `deleteVehicle()`: Calls `decrementVehicleCount()` when deleting

### 4. TestDriveFleet Updates
**Files:**
- `TestDriveFleet.java`: Removed brand/model fields, kept vehicle relationship
- `TestDriveFleetDto.java`: Removed brand/model, kept vehicleModel (derived)
- `TestDriveFleetMapper.java`: Maps vehicleModel from vehicle.brand + vehicle.model

### 5. Leads Updates (Already Done)
**Files:**
- `LeadDto.java`: Added `vehicleModel` field
- `LeadMapper.java`: Derives vehicleModel from vehicle relationship

## How It Works

### Creating a Vehicle
1. User creates vehicle with brand="Hyundai", model="Creta"
2. `VehicleService.createVehicle()` calls `VehicleModelService.getOrCreateVehicleModel("Hyundai", "Creta")`
3. If "Hyundai Creta" doesn't exist in `vehicle_models`:
   - Creates new row with vehicle_count=1
4. If it exists:
   - Increments vehicle_count by 1
5. Vehicle is created with reference to this VehicleModel

### Updating a Vehicle
1. User changes model from "Creta" to "Venue"
2. `VehicleService.updateVehicle()` calls `VehicleModelService.updateVehicleModel("Hyundai", "Creta", "Hyundai", "Venue")`
3. Decrements "Hyundai Creta" count (deactivates if reaches 0)
4. Increments or creates "Hyundai Venue" count
5. Vehicle is updated with new VehicleModel reference

### Deleting a Vehicle
1. User deletes vehicle (soft delete)
2. `VehicleService.deleteVehicle()` calls `VehicleModelService.decrementVehicleCount()`
3. Decrements vehicle_count for that model
4. If count reaches 0, sets is_active=FALSE

### Displaying Models in Other Modules

#### Leads
- Lead has optional `vehicle_id`
- LeadDto includes `vehicleModel` (derived from vehicle.brand + vehicle.model)
- Frontend displays: `vehicleModel || modelInterested || '-'`

#### Test Drive Fleet
- TestDriveFleet has required `vehicle_id`
- Only stores VIN, variant, and vehicle-specific data
- Brand/Model comes from linked vehicle
- TestDriveFleetDto includes `vehicleModel` (derived from vehicle)

#### Bookings
- Already working correctly
- Booking has `vehicle_id`
- BookingDto includes `vehicleModel` (derived from vehicle)

## Benefits

1. **Single Source of Truth**: All models come from `vehicle_models` table
2. **Automatic Sync**: When you add/edit/delete vehicles, models update automatically
3. **No Duplicates**: Unique constraint ensures one entry per brand+model
4. **Usage Tracking**: Know which models are actively in use
5. **Clean Data**: Inactive models are marked but not deleted (audit trail)
6. **Performance**: Denormalized brand/model in vehicles for fast queries
7. **Consistency**: All modules show the same model information

## Migration Strategy

The v17.0 migration:
1. Creates `vehicle_models` table
2. Populates it from existing vehicles (GROUP BY brand, model)
3. Adds `vehicle_model_id` to vehicles
4. Links existing vehicles to their models
5. Adds `vehicle_id` to test_drive_fleet
6. Removes brand/model from test_drive_fleet

## Frontend Changes Needed

### Test Drive Fleet Form
- Remove brand and model input fields
- Add vehicle selector (dropdown or autocomplete)
- Display vehicle model automatically when vehicle is selected

### Leads Form
- Already updated with optional vehicle selector
- Shows vehicleModel when vehicle is linked
- Falls back to modelInterested for general interest

## API Endpoints (Future Enhancement)

Consider adding:
```
GET /api/inventory/vehicle-models - List all active models
GET /api/inventory/vehicle-models/all - List all models (including inactive)
```

## Testing Checklist

- [ ] Create vehicle → VehicleModel created/incremented
- [ ] Update vehicle model → Old decremented, new incremented
- [ ] Delete vehicle → VehicleModel count decremented
- [ ] VehicleModel deactivated when count reaches 0
- [ ] Leads display vehicleModel from vehicle
- [ ] Test drive fleet displays vehicleModel from vehicle
- [ ] Bookings continue to work (already implemented)
- [ ] Database migration runs successfully
- [ ] No orphaned data after migration

## Notes

- Brand and model fields are kept in `vehicles` table for performance (denormalization)
- This allows fast queries without joining to `vehicle_models`
- The `vehicle_model_id` FK ensures referential integrity
- The `vehicle_count` field is for informational purposes and cleanup
