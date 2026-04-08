export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
  branchId: number | null;
  branchName: string | null;
  forcePasswordChange: boolean;
  menus: Menu[];
  permissions: Permission[];
}

export interface Role {
  id: number;
  name: RoleName;
  displayName: string;
  permissions: Permission[];
}

export type RoleName =
  | 'SUPER_ADMIN'
  | 'MASTER_USER'
  | 'SALES_CRM_EXEC'
  | 'WORKSHOP_EXEC'
  | 'MANAGER_VIEWER';

export interface Menu {
  id: number;
  name: string;
  icon: string;
  path: string;
  route?: string;
  displayOrder: number;
  parentId: number | null;
  children: Menu[];
}

export interface Permission {
  id: number;
  moduleName: string;
  canCreate: boolean;
  canRead: boolean;
  canUpdate: boolean;
  canDelete: boolean;
}
