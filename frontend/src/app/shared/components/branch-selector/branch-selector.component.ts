import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { BranchContextService } from '../../../core/services/branch-context.service';
import { AdminService } from '../../../modules/admin/services/admin.service';
import { BranchDto } from '../../../modules/admin/models/admin.model';

@Component({
  selector: 'app-branch-selector',
  template: `
    <div class="branch-selector" *ngIf="isAdmin">
      <mat-icon class="branch-icon">store</mat-icon>
      <mat-form-field appearance="outline" class="branch-dropdown">
        <mat-select
          [value]="selectedBranchId"
          (selectionChange)="onBranchChange($event.value)"
          placeholder="All Branches">
          <mat-option [value]="null">All Branches</mat-option>
          <mat-option *ngFor="let branch of branches" [value]="branch.id">
            {{ branch.name }}
          </mat-option>
        </mat-select>
      </mat-form-field>
    </div>
  `,
  styles: [`
    .branch-selector {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-right: 8px;
    }

    .branch-icon {
      color: var(--dms-accent, #1976d2);
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .branch-dropdown {
      width: 200px;

      ::ng-deep .mat-mdc-form-field-subscript-wrapper {
        display: none;
      }

      ::ng-deep .mat-mdc-text-field-wrapper {
        height: 36px !important;
        padding: 0 8px !important;
      }

      ::ng-deep .mat-mdc-form-field-infix {
        padding: 4px 0 !important;
        min-height: 36px !important;
      }

      ::ng-deep .mat-mdc-select-trigger {
        font-size: 13px;
        font-weight: 500;
      }

      ::ng-deep .mdc-notched-outline__leading,
      ::ng-deep .mdc-notched-outline__trailing,
      ::ng-deep .mdc-notched-outline__notch {
        border-color: var(--dms-card-border, rgba(0,0,0,0.12)) !important;
      }
    }
  `]
})
export class BranchSelectorComponent implements OnInit, OnDestroy {
  branches: BranchDto[] = [];
  selectedBranchId: number | null = null;
  isAdmin = false;
  private destroy$ = new Subject<void>();

  constructor(
    private branchContextService: BranchContextService,
    private adminService: AdminService,
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.branchContextService.isAdmin();

    if (this.isAdmin) {
      // Load branches for dropdown
      this.adminService.getAllBranches().subscribe({
        next: (res) => {
          this.branches = (res.data || []).filter((b: BranchDto) => b.isActive);
        },
      });

      // Subscribe to branch context changes
      this.branchContextService.activeBranchId$
        .pipe(takeUntil(this.destroy$))
        .subscribe((branchId) => {
          this.selectedBranchId = branchId;
        });
    }
  }

  onBranchChange(branchId: number | null): void {
    this.branchContextService.setActiveBranch(branchId);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
