# Multi-Branch Architecture + Admin Module Cleanup

Implement branch-scoped data access across all modules, add branch CRUD, seed multi-branch users, fix role management, and remove code management.

## User Review Required

> [!IMPORTANT]
> **New Users & Credentials**: I'll create the following users across 3 branches. Please confirm this is what you want:
>
> | Branch | Role | Username | Password |
> |--------|------|----------|----------|
> | **Hyderabad** (ID 1) | SUPER_ADMIN | `admin` | `Admin@123` (existing) |
> | **Hyderabad** | SALES_CRM_EXEC | `sales_hyd_1` | `Sales@123` |
> | **Hyderabad** | SALES_CRM_EXEC | `sales_hyd_2` | `Sales@123` |
> | **Hyderabad** | WORKSHOP_EXEC | `workshop_hyd_1` | `Workshop@123` |
> | **Hyderabad** | WORKSHOP_EXEC | `workshop_hyd_2` | `Workshop@123` |
> | **Hyderabad** | MANAGER_VIEWER | `manager_hyd_1` | `Manager@123` |
> | **Hyderabad** | MANAGER_VIEWER | `manager_hyd_2` | `Manager@123` |
> | **Hyderabad** | MASTER_USER | `master_hyd_1` | `Master@123` |
> | **Hyderabad** | MASTER_USER | `master_hyd_2` | `Master@123` |
> | **Mumbai** (ID 2) | SALES_CRM_EXEC | `sales_mum_1` | `Sales@123` |
> | **Mumbai** | SALES_CRM_EXEC | `sales_mum_2` | `Sales@123` |
> | **Mumbai** | WORKSHOP_EXEC | `workshop_mum_1` | `Workshop@123` |
> | **Mumbai** | WORKSHOP_EXEC | `workshop_mum_2` | `Workshop@123` |
> | **Mumbai** | MANAGER_VIEWER | `manager_mum_1` | `Manager@123` |
> | **Mumbai** | MANAGER_VIEWER | `manager_mum_2` | `Manager@123` |
> | **Mumbai** | MASTER_USER | `master_mum_1` | `Master@123` |
> | **Mumbai** | MASTER_USER | `master_mum_2` | `Master@123` |
> | **Chennai** (ID 3) | SALES_CRM_EXEC | `sales_chn_1` | `Sales@123` |
> | **Chennai** | SALES_CRM_EXEC | `sales_chn_2` | `Sales@123` |
> | **Chennai** | WORKSHOP_EXEC | `workshop_chn_1` | `Workshop@123` |
> | **Chennai** | WORKSHOP_EXEC | `workshop_chn_2` | `Workshop@123` |
> | **Chennai** | MANAGER_VIEWER | `manager_chn_1` | `Manager@123` |
> | **Chennai** | MANAGER_VIEWER | `manager_chn_2` | `Manager@123` |
> | **Chennai** | MASTER_USER | `master_chn_1` | `Master@123` |
> | **Chennai** | MASTER_USER | `master_chn_2` | `Master@123` |

> [!WARNING]
> **Admin branch**: The `admin` user (SUPER_ADMIN) will have `branchId = NULL` — they're not tied to any specific branch but can select any branch via dropdown. All non-admin users are locked to their assigned branch.

> [!IMPORTANT]
> **Removing Code Management**: Both frontend (component, route, module declaration) and the "Code Management" menu item from the sidebar will be removed. The backend `CodeController` and related classes will be kept intact in case they're needed later via API — only the frontend UI is removed. Confirm if you also want the backend code removed.

---

## Proposed Changes

### Component 1: Seed Data (Branches + Users)

#### [MODIFY] [data.sql](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/backend/src/main/resources/db/changelog/v1.0/data.sql)
- Update Hyderabad branch name: `Hyundai Hyderabad Central`
- Add Mumbai branch (ID 2): `Hyundai Mumbai Central` (already partially exists in v8 data, will be inserted properly here)
- Add Chennai branch (ID 3): `Hyundai Chennai Central`
- Set admin `branch_id = NULL` (not tied to any branch)
- Add 24 new users (2 per role × 4 roles × 3 branches: MASTER_USER, SALES_CRM_EXEC, WORKSHOP_EXEC, MANAGER_VIEWER)
- Remove `Code Management` menu entry (ID 14) from menus & role_menus

---

### Component 2: Branch Management CRUD (Backend)

#### [MODIFY] [BranchController.java](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/backend/src/main/java/com/hyundai/dms/module/user/controller/BranchController.java)
- Add `POST /api/admin/branches` — create branch
- Add `PUT /api/admin/branches/{id}` — update branch
- Add `DELETE /api/admin/branches/{id}` — delete (soft deactivate) branch
- Add `@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")` to CUD endpoints

#### [NEW] [CreateBranchRequest.java](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/backend/src/main/java/com/hyundai/dms/module/user/dto/CreateBranchRequest.java)
- DTO with fields: `code`, `name`, `region`, `gstin`

#### [NEW] [UpdateBranchRequest.java](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/backend/src/main/java/com/hyundai/dms/module/user/dto/UpdateBranchRequest.java)
- DTO with fields: `name`, `region`, `gstin`, `isActive`

---

### Component 3: Branch Management CRUD (Frontend)

#### [MODIFY] [branch-management.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/components/branch-management/branch-management.component.ts)
- Add create/edit drawer with form (code, name, region, gstin, isActive)
- Add edit and delete action buttons per row
- Integrate with new API endpoints

#### [MODIFY] [branch-management.component.html](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/components/branch-management/branch-management.component.html)
- Add "New Branch" button in header
- Add actions column to table
- Add create/edit drawer form

#### [MODIFY] [branch-management.component.scss](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/components/branch-management/branch-management.component.scss)
- Add drawer and actions styles

#### [MODIFY] [admin.service.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/services/admin.service.ts)
- Add `createBranch()`, `updateBranch()`, `deleteBranch()` methods

#### [MODIFY] [admin.model.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/models/admin.model.ts)
- Add `CreateBranchRequest` and `UpdateBranchRequest` interfaces

---

### Component 4: Branch Context Service (Core)

#### [NEW] [branch-context.service.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/core/services/branch-context.service.ts)
- Central service that exposes `activeBranchId$` as a `BehaviorSubject`
- On init, reads user's role from NgRx store:
  - **SUPER_ADMIN**: starts with `null` (all branches), user can pick from dropdown
  - **Other roles**: locked to `user.branchId`, cannot change
- Methods: `setActiveBranch(id)`, `getActiveBranchId()`, `isAdmin()`
- All modules will inject this service to get the branch filter

---

### Component 5: Branch Selector Component (Shared)

#### [NEW] [branch-selector.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/shared/components/branch-selector/branch-selector.component.ts)
- Dropdown component that shows all active branches
- Only visible for SUPER_ADMIN role
- Emits `branchChange` event when selection changes
- Updates `BranchContextService` on change

This will be placed in the **app header/toolbar** (not per-module) so it's always visible for admins.

---

### Component 6: Integrate Branch Filtering into Modules

The approach differs by how each module loads data:

**Modules using `DataTableComponent` with `/filter` POST API** (Inventory vehicles, GRN, Leads, Test Drive fleet/bookings):
- Inject `BranchContextService`
- Auto-inject a `branchId` filter into the `FilterRequest.filters` array before sending to API
- The backend specification builder already supports `branchId` as a filter field on these entities

**Modules using direct `getByBranch(branchId)` APIs** (Service bookings, Service records, Payments):
- Replace hardcoded `branchId = 1` with `BranchContextService.getActiveBranchId()`
- Subscribe to branch changes to reload data

#### Files to modify:

| File | Change |
|------|--------|
| [vehicle-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/inventory/components/vehicle-list/vehicle-list.component.ts) | Add branch filter injection |
| [lead-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/sales/components/lead-list/lead-list.component.ts) | Add branch filter to loadLeads() |
| [service-booking-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/service/components/service-booking-list/service-booking-list.component.ts) | Use BranchContextService instead of hardcoded `1` |
| [service-record-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/service/components/service-record-list/service-record-list.component.ts) | Use BranchContextService |
| [service-booking-form.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/service/components/service-booking-form/service-booking-form.component.ts) | Use BranchContextService |
| [service-record-form.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/service/components/service-record-form/service-record-form.component.ts) | Use BranchContextService |
| [payment-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/sales/components/payment-list/payment-list.component.ts) | Use BranchContextService |
| [fleet-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/testdrive/components/fleet-list/fleet-list.component.ts) | Add branch filter |
| [booking-list.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/testdrive/components/booking-list/booking-list.component.ts) | Add branch filter |

---

### Component 7: Add Branch Selector to App Header

#### [MODIFY] [app.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/app.component.ts)
- Add `<app-branch-selector>` in the toolbar area (visible only for SUPER_ADMIN)

---

### Component 8: Remove Code Management Frontend

#### [MODIFY] [admin.module.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/admin.module.ts)
- Remove `CodeManagementComponent` import, declaration, and route

#### [DELETE] [code-management/](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/components/code-management/)
- Delete entire component directory

#### [MODIFY] [admin.model.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/models/admin.model.ts)
- Remove `CodeDto` and `CreateCodeRequest` interfaces

#### [MODIFY] [admin.service.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/services/admin.service.ts)
- Remove all code-related methods and imports

---

### Component 9: Fix Role Permission Module

The permission matrix works with flat `{module, action}` pairs from the backend, but the frontend `ADMIN_MODULES` constant doesn't match the backend `module_name` values. The backend uses: `USER_MANAGEMENT`, `ROLE_MANAGEMENT`, `INVENTORY_MANAGEMENT`, `SALES_MANAGEMENT`, `REPORTS`, `BRANCH_MANAGEMENT`. The frontend uses: `INVENTORY`, `SALES`, `NOTIFICATION`, `USER`, `REPORTS`.

#### [MODIFY] [admin.model.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/models/admin.model.ts)
- Fix `ADMIN_MODULES` to match backend values: `['INVENTORY_MANAGEMENT', 'SALES_MANAGEMENT', 'USER_MANAGEMENT', 'ROLE_MANAGEMENT', 'BRANCH_MANAGEMENT', 'REPORTS']`

#### [MODIFY] [role-permission.component.ts](file:///c:/Users/Lenovo/Desktop/comprehensive-inventory-management-main/frontend/src/app/modules/admin/components/role-permission/role-permission.component.ts)
- Fix `buildPermissionMatrix()` to correctly match permissions by `module` field from backend (the DTO flattens to `{module, action}` pairs)

---

## Open Questions

> [!IMPORTANT]
> 1. Should the SUPER_ADMIN user be assigned to `branch_id = NULL` (no specific branch, sees all) or remain on Hyderabad but still see the global dropdown?
> 2. Should the backend `CodeController` and code tables also be removed, or just the frontend UI?
> 3. For the role permission module — the `ADMIN_MODULES` list mismatches with backend `module_name` values. I'll fix this by aligning them. Is that acceptable?

---

## Verification Plan

### Automated Tests
- `mvn spring-boot:run` — verify backend starts without errors
- `npm start` — verify frontend compiles without errors
- Test branch CRUD via the branch management UI
- Login as admin → verify branch dropdown appears in toolbar
- Login as `sales_hyd_1` → verify NO branch dropdown, data shows only Hyderabad
- Test role permission save → verify no 500 errors

### Manual Verification
- User should test login with each role to verify branch-scoped data
- Verify Code Management is no longer accessible in the sidebar
