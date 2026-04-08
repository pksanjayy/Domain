# Vehicle Model Dropdown with Search Implementation

## Overview
Implemented a searchable dropdown component for vehicle models that can be used across all forms where model input is required. The dropdown fetches data from the centralized VehicleModel table.

## Backend Implementation

### 1. VehicleModelDto
**File:** `backend/src/main/java/com/hyundai/dms/module/inventory/dto/VehicleModelDto.java`

```java
{
  id: Long,
  brand: String,
  model: String,
  displayName: String,  // "Brand Model" for display
  isActive: Boolean,
  vehicleCount: Integer
}
```

### 2. VehicleModelMapper
**File:** `backend/src/main/java/com/hyundai/dms/module/inventory/mapper/VehicleModelMapper.java`

Maps VehicleModel entity to DTO with auto-generated `displayName`.

### 3. VehicleModelController
**File:** `backend/src/main/java/com/hyundai/dms/module/inventory/controller/VehicleModelController.java`

**Endpoints:**
- `GET /api/inventory/vehicle-models` - Returns active models only
- `GET /api/inventory/vehicle-models/all` - Returns all models (including inactive)

### 4. Updated VehicleModelService
Added DTO methods:
- `getAllActiveModelsDto()` - Returns List<VehicleModelDto> of active models
- `getAllModelsDto()` - Returns List<VehicleModelDto> of all models

## Frontend Implementation

### 1. VehicleModelService
**File:** `frontend/src/app/core/services/vehicle-model.service.ts`

Angular service to fetch vehicle models from API.

**Methods:**
- `getActiveModels()`: Observable<VehicleModelDto[]>
- `getAllModels()`: Observable<VehicleModelDto[]>

### 2. VehicleModelSelectorComponent
**File:** `frontend/src/app/shared/components/vehicle-model-selector/`

Reusable component with search functionality.

**Features:**
- Autocomplete with search
- Filters by brand, model, or display name
- Shows vehicle count for each model
- Implements ControlValueAccessor (works with Angular forms)
- Configurable appearance and labels

**Inputs:**
- `label`: string - Field label (default: "Vehicle Model")
- `placeholder`: string - Placeholder text
- `required`: boolean - Whether field is required
- `appearance`: 'fill' | 'outline' - Material appearance
- `includeInactive`: boolean - Include inactive models

**Usage Example:**
```html
<app-vehicle-model-selector
  [label]="'Vehicle Model'"
  [required]="true"
  formControlName="vehicleModel">
</app-vehicle-model-selector>
```

### 3. Updated Vehicle Form
**File:** `frontend/src/app/modules/inventory/components/vehicle-form/`

**Features:**
- Toggle between "Select from existing" and "Enter new"
- When "Select from existing" is enabled:
  - Shows searchable dropdown of existing models
  - Auto-populates brand and model fields
- When disabled:
  - Shows manual brand and model input fields
  - Allows creating new models

**Benefits:**
- Encourages reuse of existing models
- Reduces typos and inconsistencies
- Still allows creating new models when needed

## How It Works

### User Flow - Vehicle Form

1. **Creating a Vehicle:**
   - User toggles "Select from existing models"
   - Dropdown shows all active models with search
   - User types "Creta" → filters to show "Hyundai Creta", "Hyundai Creta N Line", etc.
   - User selects "Hyundai Creta"
   - Brand and Model fields auto-populate
   - User continues with other fields

2. **Creating a New Model:**
   - User keeps toggle OFF
   - Manually enters Brand: "Hyundai", Model: "Alcazar"
   - On save, VehicleModelService creates new entry
   - Future users can select "Hyundai Alcazar" from dropdown

### Search Functionality

The dropdown filters by:
- Brand name (e.g., "Hyundai")
- Model name (e.g., "Creta")
- Display name (e.g., "Hyundai Creta")

**Example Searches:**
- Type "creta" → Shows all Creta variants
- Type "hyundai" → Shows all Hyundai models
- Type "venue" → Shows Hyundai Venue

## Integration Points

### Where to Use This Component

1. **Vehicle Form** ✅ Implemented
   - Toggle between selector and manual input
   
2. **Lead Form** (Future)
   - Replace `modelInterested` text input with dropdown
   
3. **Test Drive Fleet Form** (Future)
   - Use dropdown when selecting vehicle model

4. **Any Form Requiring Model Input**
   - Import SharedModule
   - Add `<app-vehicle-model-selector>` to template

## Component API

### VehicleModelSelectorComponent

**Selector:** `app-vehicle-model-selector`

**Inputs:**
```typescript
@Input() label: string = 'Vehicle Model';
@Input() placeholder: string = 'Select or search model';
@Input() required: boolean = false;
@Input() appearance: 'fill' | 'outline' = 'outline';
@Input() includeInactive: boolean = false;
```

**Implements:** `ControlValueAccessor`

**Value Format:**
```typescript
{
  brand: string,
  model: string
}
```

## Styling

The component includes:
- Material Design autocomplete
- Search icon
- Vehicle count badge
- Highlighted brand names
- Responsive layout

## Benefits

1. **Consistency**: All users select from same model list
2. **Search**: Fast filtering by any part of the name
3. **Validation**: Only valid models can be selected
4. **Reusability**: One component used everywhere
5. **User Experience**: Autocomplete is faster than typing
6. **Data Quality**: Reduces typos and duplicates
7. **Flexibility**: Can still create new models when needed

## Testing Checklist

- [ ] Dropdown loads active models
- [ ] Search filters correctly
- [ ] Selecting a model populates brand/model fields
- [ ] Toggle switches between selector and manual input
- [ ] Creating new vehicle with new model works
- [ ] Creating new vehicle with existing model works
- [ ] Vehicle count displays correctly
- [ ] Component works in reactive forms
- [ ] Required validation works
- [ ] Disabled state works

## Future Enhancements

1. **Add to Lead Form**
   - Replace modelInterested with dropdown
   - Keep text field as fallback

2. **Add to Test Drive Fleet Form**
   - Use dropdown for model selection

3. **Add Model Images**
   - Show vehicle thumbnail in dropdown

4. **Add Variant Selector**
   - Cascade: Select Model → Select Variant

5. **Add Recent Models**
   - Show recently used models at top

6. **Add Favorites**
   - Allow users to favorite frequently used models
