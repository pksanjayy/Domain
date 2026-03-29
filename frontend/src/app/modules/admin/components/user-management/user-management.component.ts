import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AdminService } from '../../services/admin.service';
import {
  UserListDto,
  CreateUserRequest,
  UpdateUserRequest,
  RoleDetailDto,
  BranchDto,
} from '../../models/admin.model';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss'],
})
export class UserManagementComponent implements OnInit {
  displayedColumns = [
    'username', 'email', 'roleName', 'branchName', 'status',
    'failedLoginAttempts', 'createdAt', 'actions'
  ];
  dataSource = new MatTableDataSource<UserListDto>([]);
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  isLoading = false;
  searchQuery = '';

  // Drawer
  isDrawerOpen = false;
  isEditMode = false;
  editingUser: UserListDto | null = null;
  userForm!: FormGroup;
  hidePassword = true;
  isSubmitting = false;

  // Dropdown data
  roles: RoleDetailDto[] = [];
  branches: BranchDto[] = [];

  // Filter
  filterRole = '';
  filterBranch = '';
  filterStatus = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDropdowns();
    this.loadUsers();
  }

  initForm(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      roleId: [null, Validators.required],
      branchId: [null],
      isActive: [true],
    });
  }

  loadDropdowns(): void {
    this.adminService.getRoles().subscribe({
      next: (res) => (this.roles = res.data),
    });
    this.adminService.getBranches().subscribe({
      next: (res) => (this.branches = res.data),
    });
  }

  loadUsers(): void {
    this.isLoading = true;
    this.adminService.getUsers(this.searchQuery, this.pageIndex, this.pageSize).subscribe({
      next: (res) => {
        this.dataSource.data = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onSearch(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value;
    this.pageIndex = 0;
    this.loadUsers();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  onSortChange(sort: Sort): void {
    this.loadUsers();
  }

  // ─── Drawer ───
  openCreateDrawer(): void {
    this.isEditMode = false;
    this.editingUser = null;
    this.userForm.reset({ isActive: true });
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(8)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.userForm.get('username')?.enable();
    this.isDrawerOpen = true;
  }

  openEditDrawer(user: UserListDto): void {
    this.isEditMode = true;
    this.editingUser = user;
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      roleId: user.roleId,
      branchId: user.branchId,
      isActive: user.isActive,
    });
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.userForm.get('username')?.disable();
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
    this.editingUser = null;
  }

  onSubmitUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    if (this.isEditMode && this.editingUser) {
      const request: UpdateUserRequest = {
        roleId: this.userForm.value.roleId,
        branchId: this.userForm.value.branchId,
        isActive: this.userForm.value.isActive,
      };
      this.adminService.updateUser(this.editingUser.id, request).subscribe({
        next: () => {
          this.snackBar.open('User updated successfully', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadUsers();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    } else {
      const request: CreateUserRequest = {
        username: this.userForm.value.username,
        email: this.userForm.value.email,
        password: this.userForm.value.password,
        roleId: this.userForm.value.roleId,
        branchId: this.userForm.value.branchId,
        isActive: this.userForm.value.isActive,
      };
      this.adminService.createUser(request).subscribe({
        next: () => {
          this.snackBar.open('User created successfully', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadUsers();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    }
  }

  // ─── Lock/Unlock ───
  toggleLock(user: UserListDto): void {
    const isLocked = !!user.lockedAt;
    const action = isLocked ? 'unlock' : 'lock';

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${isLocked ? 'Unlock' : 'Lock'} User`,
        message: `Are you sure you want to ${action} user "${user.username}"?`,
        confirmText: isLocked ? 'Unlock' : 'Lock',
        confirmColor: isLocked ? 'primary' : 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.adminService.lockUnlockUser(user.id, !isLocked).subscribe({
          next: () => {
            this.snackBar.open(`User ${action}ed successfully`, 'Close', { duration: 3000 });
            this.loadUsers();
          },
        });
      }
    });
  }

  // ─── Reset Password ───
  resetPassword(user: UserListDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Reset Password',
        message: `Reset password for "${user.username}"? A temporary password will be generated.`,
        confirmText: 'Reset',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.adminService.resetPassword(user.id).subscribe({
          next: (res) => {
            this.snackBar.open(
              `Temporary password: ${res.data.temporaryPassword}`,
              'Copy',
              { duration: 15000 }
            );
            this.loadUsers();
          },
        });
      }
    });
  }

  getStatusLabel(user: UserListDto): string {
    if (user.lockedAt) return 'Locked';
    return user.isActive ? 'Active' : 'Inactive';
  }

  getStatusColor(user: UserListDto): string {
    if (user.lockedAt) return 'warn';
    return user.isActive ? 'primary' : 'accent';
  }

  getRoleBadgeClass(roleName: string): string {
    const map: Record<string, string> = {
      SUPER_ADMIN: 'role-super-admin',
      MASTER_USER: 'role-master-user',
      SALES_CRM_EXEC: 'role-sales',
      WORKSHOP_EXEC: 'role-workshop',
      MANAGER_VIEWER: 'role-manager',
    };
    return map[roleName] || 'role-default';
  }
}
