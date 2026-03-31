import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { AdminService } from '../../services/admin.service';
import { BranchDto } from '../../models/admin.model';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-branch-management',
  templateUrl: './branch-management.component.html',
  styleUrls: ['./branch-management.component.scss'],
})
export class BranchManagementComponent implements OnInit {
  displayedColumns = ['code', 'name', 'region', 'gstin', 'status', 'actions'];
  dataSource = new MatTableDataSource<BranchDto>([]);
  isLoading = false;
  searchQuery = '';
  filterStatus = '';
  
  // Pagination
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;

  // Drawer state
  isDrawerOpen = false;
  isEditMode = false;
  editingBranchId: number | null = null;
  branchForm!: FormGroup;
  isSubmitting = false;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBranches();
  }

  initForm(): void {
    this.branchForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(20)]],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      region: [''],
      gstin: [''],
      isActive: [true],
    });
  }

  loadBranches(): void {
    this.isLoading = true;
    const isActive = this.filterStatus === '' ? undefined : this.filterStatus === 'true';
    this.adminService.getBranches({
      search: this.searchQuery,
      isActive: isActive,
      page: this.pageIndex,
      size: this.pageSize
    }).subscribe({
      next: (res) => {
        this.dataSource.data = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load branches', 'Close', { duration: 3000 });
        this.isLoading = false;
      },
    });
  }

  onSearch(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value;
    this.pageIndex = 0;
    this.loadBranches();
  }

  onStatusChange(value: string): void {
    this.filterStatus = value;
    this.pageIndex = 0;
    this.loadBranches();
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadBranches();
  }

  openCreateDrawer(): void {
    this.isEditMode = false;
    this.editingBranchId = null;
    this.branchForm.reset({ isActive: true });
    this.branchForm.get('code')?.enable();
    this.isDrawerOpen = true;
  }

  openEditDrawer(branch: BranchDto): void {
    this.isEditMode = true;
    this.editingBranchId = branch.id;
    this.branchForm.patchValue({
      code: branch.code,
      name: branch.name,
      region: branch.region || '',
      gstin: branch.gstin || '',
      isActive: branch.isActive,
    });
    this.branchForm.get('code')?.disable();
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
  }

  onSubmit(): void {
    if (this.branchForm.invalid) {
      this.branchForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.branchForm.getRawValue();

    if (this.isEditMode && this.editingBranchId) {
      const updatePayload = {
        name: formValue.name,
        region: formValue.region || null,
        gstin: formValue.gstin || null,
        isActive: formValue.isActive,
      };

      this.adminService.updateBranch(this.editingBranchId, updatePayload).subscribe({
        next: () => {
          this.snackBar.open('Branch updated successfully', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadBranches();
          this.isSubmitting = false;
        },
        error: () => {
          this.snackBar.open('Failed to update branch', 'Close', { duration: 3000 });
          this.isSubmitting = false;
        },
      });
    } else {
      const createPayload = {
        code: formValue.code,
        name: formValue.name,
        region: formValue.region || null,
        gstin: formValue.gstin || null,
      };

      this.adminService.createBranch(createPayload).subscribe({
        next: () => {
          this.snackBar.open('Branch created successfully', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadBranches();
          this.isSubmitting = false;
        },
        error: () => {
          this.snackBar.open('Failed to create branch', 'Close', { duration: 3000 });
          this.isSubmitting = false;
        },
      });
    }
  }

  deleteBranch(branch: BranchDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Deactivate Branch',
        message: `Are you sure you want to deactivate "${branch.name}"? Users assigned to this branch will lose access.`,
        confirmText: 'Deactivate',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.adminService.deleteBranch(branch.id).subscribe({
          next: () => {
            this.snackBar.open('Branch deactivated', 'Close', { duration: 3000 });
            this.loadBranches();
          },
          error: () => {
            this.snackBar.open('Failed to deactivate branch', 'Close', { duration: 3000 });
          },
        });
      }
    });
  }

  getStatusLabel(branch: BranchDto): string {
    return branch.isActive !== false ? 'Active' : 'Inactive';
  }

  getStatusColor(branch: BranchDto): string {
    return branch.isActive !== false ? 'primary' : 'accent';
  }
}
