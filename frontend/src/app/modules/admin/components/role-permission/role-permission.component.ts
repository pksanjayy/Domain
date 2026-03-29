import { Component, OnInit } from '@angular/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../services/admin.service';
import {
  RoleDetailDto,
  PermissionMatrixEntry,
  PermissionEntry,
  MenuTreeNode,
  ADMIN_MODULES,
  PERMISSION_ACTIONS,
} from '../../models/admin.model';

interface FlatMenuNode {
  id: number;
  name: string;
  level: number;
  expandable: boolean;
  isSelected: boolean;
}

@Component({
  selector: 'app-role-permission',
  templateUrl: './role-permission.component.html',
  styleUrls: ['./role-permission.component.scss'],
})
export class RolePermissionComponent implements OnInit {
  roles: RoleDetailDto[] = [];
  selectedRole: RoleDetailDto | null = null;
  permissionMatrix: PermissionMatrixEntry[] = [];
  displayedPermColumns = ['module', ...PERMISSION_ACTIONS];
  modules = ADMIN_MODULES;
  actions = PERMISSION_ACTIONS;
  isSaving = false;
  hasChanges = false;

  // Menu Tree
  allMenus: MenuTreeNode[] = [];
  selectedMenuIds: Set<number> = new Set();

  treeControl: FlatTreeControl<FlatMenuNode>;
  treeFlattener: MatTreeFlattener<MenuTreeNode, FlatMenuNode>;
  menuDataSource: MatTreeFlatDataSource<MenuTreeNode, FlatMenuNode>;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar,
  ) {
    this.treeFlattener = new MatTreeFlattener(
      (node: MenuTreeNode, level: number): FlatMenuNode => ({
        id: node.id,
        name: node.name,
        level: level,
        expandable: node.children && node.children.length > 0,
        isSelected: false as boolean,
      }),
      (node) => node.level || 0,
      (node) => node.expandable || false,
      (node) => node.children,
    );
    this.treeControl = new FlatTreeControl<FlatMenuNode>(
      (node) => node.level,
      (node) => node.expandable,
    );
    this.menuDataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  }

  ngOnInit(): void {
    this.loadRoles();
    this.loadMenus();
  }

  loadRoles(): void {
    this.adminService.getRoles().subscribe({
      next: (res) => {
        this.roles = res.data;
        if (this.roles.length > 0 && !this.selectedRole) {
          this.selectRole(this.roles[0]);
        }
      },
    });
  }

  loadMenus(): void {
    this.adminService.getAllMenus().subscribe({
      next: (res) => {
        this.allMenus = res.data;
        this.menuDataSource.data = this.allMenus;
        this.treeControl.expandAll();
      },
    });
  }

  selectRole(role: RoleDetailDto): void {
    this.selectedRole = role;
    this.hasChanges = false;
    this.buildPermissionMatrix(role.permissions);
    this.buildMenuSelection(role.menuIds || []);
  }

  buildPermissionMatrix(permissions: PermissionEntry[]): void {
    this.permissionMatrix = this.modules.map((mod) => {
      const entry: PermissionMatrixEntry = {
        module: mod,
        create: false,
        read: false,
        update: false,
        delete: false,
      };
      permissions.forEach((p) => {
        if (p.module === mod) {
          const action = p.action.toLowerCase() as 'create' | 'read' | 'update' | 'delete';
          if (action in entry) {
            (entry as any)[action] = true;
          }
        }
      });
      return entry;
    });
  }

  buildMenuSelection(menuIds: number[]): void {
    this.selectedMenuIds = new Set(menuIds);
    // Update tree nodes
    if (this.treeControl.dataNodes) {
      this.treeControl.dataNodes.forEach((node) => {
        node.isSelected = this.selectedMenuIds.has(node.id);
      });
    }
  }

  togglePermission(row: PermissionMatrixEntry, action: string): void {
    const key = action.toLowerCase() as 'create' | 'read' | 'update' | 'delete';
    (row as any)[key] = !(row as any)[key];
    this.hasChanges = true;
  }

  isPermissionChecked(row: PermissionMatrixEntry, action: string): boolean {
    return (row as any)[action.toLowerCase()];
  }

  toggleMenuNode(node: FlatMenuNode): void {
    node.isSelected = !node.isSelected;
    if (node.isSelected) {
      this.selectedMenuIds.add(node.id);
    } else {
      this.selectedMenuIds.delete(node.id);
    }
    this.hasChanges = true;
  }

  hasChild = (_: number, node: FlatMenuNode) => node.expandable;

  saveChanges(): void {
    if (!this.selectedRole) return;

    this.isSaving = true;
    const roleId = this.selectedRole.id;

    // Build permissions list from matrix
    const permissions: PermissionEntry[] = [];
    this.permissionMatrix.forEach((row) => {
      this.actions.forEach((action) => {
        if ((row as any)[action.toLowerCase()]) {
          permissions.push({
            id: 0,
            module: row.module,
            action: action,
          });
        }
      });
    });

    // Save permissions
    this.adminService.updateRolePermissions(roleId, permissions).subscribe({
      next: () => {
        // Save menu access
        const menuIds = Array.from(this.selectedMenuIds);
        this.adminService.updateRoleMenus(roleId, menuIds).subscribe({
          next: (res) => {
            this.snackBar.open('Role configuration saved successfully', 'Close', { duration: 3000 });
            this.hasChanges = false;
            this.isSaving = false;
            this.loadRoles();
          },
          error: () => (this.isSaving = false),
        });
      },
      error: () => (this.isSaving = false),
    });
  }
}
