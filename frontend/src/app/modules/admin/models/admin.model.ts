import { RoleName } from '../../../core/models/user.model';

// ─── User Management ───
export interface UserListDto {
  id: number;
  username: string;
  email: string;
  roleName: string;
  roleId: number;
  branchName: string | null;
  branchId: number | null;
  isActive: boolean;
  failedLoginAttempts: number;
  lockedAt: string | null;
  forcePasswordChange: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roleId: number;
  branchId: number | null;
  isActive: boolean;
}

export interface UpdateUserRequest {
  roleId: number;
  branchId: number | null;
  isActive: boolean;
}

// ─── Roles ───
export interface RoleDetailDto {
  id: number;
  name: RoleName;
  displayName: string;
  permissions: PermissionEntry[];
  menuIds: number[];
}

export interface PermissionEntry {
  id: number;
  module: string;
  action: string;
}

export interface PermissionMatrixEntry {
  module: string;
  create: boolean;
  read: boolean;
  update: boolean;
  delete: boolean;
}

// ─── Menu ───
export interface MenuTreeNode {
  id: number;
  name: string;
  icon: string;
  path: string;
  displayOrder: number;
  parentId: number | null;
  children: MenuTreeNode[];
  level?: number;
  expandable?: boolean;
  isSelected?: boolean;
}

export interface CreateMenuRequest {
  name: string;
  icon: string;
  path: string;
  displayOrder: number;
  parentId: number | null;
}

export interface UpdateMenuRequest {
  name: string;
  icon: string;
  path: string;
  displayOrder: number;
  parentId: number | null;
}

// ─── Audit Log ───
export interface AuditLogDto {
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

// ─── Branch ───
export interface BranchDto {
  id: number;
  name: string;
  code: string;
  region: string | null;
  gstin: string | null;
  isActive: boolean;
}

export interface CreateBranchRequest {
  code: string;
  name: string;
  region: string | null;
  gstin: string | null;
}

export interface UpdateBranchRequest {
  name: string;
  region: string | null;
  gstin: string | null;
  isActive: boolean;
}

// ─── Module names for permission matrix (must match backend module_name values) ───
export const ADMIN_MODULES = [
  'INVENTORY_MANAGEMENT', 'SALES_MANAGEMENT', 'USER_MANAGEMENT',
  'ROLE_MANAGEMENT', 'BRANCH_MANAGEMENT', 'REPORTS'
];

export const PERMISSION_ACTIONS = ['CREATE', 'READ', 'UPDATE', 'DELETE'];
