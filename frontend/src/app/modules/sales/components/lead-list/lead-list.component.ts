import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { SalesService } from '../../services/sales.service';
import { LeadDto, CreateLeadRequest, LeadStage } from '../../models/sales.model';
import { FilterRequest } from '../../../../core/models';
import { BranchContextService } from '../../../../core/services/branch-context.service';
import { AdminService } from '../../../admin/services/admin.service';
import { BranchDto, UserListDto } from '../../../admin/models/admin.model';


@Component({
  selector: 'app-lead-list',
  templateUrl: './lead-list.component.html',
  styleUrls: ['./lead-list.component.scss'],
})
export class LeadListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  displayedColumns = [
    'customerName', 'customerMobile', 'modelInterested', 'source',
    'stage', 'assignedToUsername', 'branchName', 'createdAt', 'actions'
  ];
  dataSource = new MatTableDataSource<LeadDto>([]);
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  isLoading = false;

  isDrawerOpen = false;
  leadForm!: FormGroup;
  isSubmitting = false;

  branches: BranchDto[] = [];
  users: UserListDto[] = [];

  stages: LeadStage[] = ['NEW_LEAD', 'TEST_DRIVE', 'QUOTATION', 'BOOKING', 'DELIVERY_READY', 'DELIVERED', 'LOST'];


  constructor(
    private salesService: SalesService,
    private adminService: AdminService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private branchContext: BranchContextService,
  ) {}


  ngOnInit(): void {
    this.initForm();
    this.loadLeads();
    this.loadLookupData();

    // Reload when branch changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadLeads());
  }

  loadLookupData(): void {
    this.adminService.getBranches().subscribe(res => this.branches = res.data);
    // Loading users (could be optimized to only sales users later)
    this.adminService.getUsers(undefined, 0, 100).subscribe(res => this.users = res.data.content);
  }


  initForm(): void {
    this.leadForm = this.fb.group({
      customerId: [null, Validators.required],
      assignedToId: [null],
      modelInterested: ['', Validators.required],
      source: ['', Validators.required],
      branchId: [null, Validators.required],
    });
  }


  loadLeads(): void {
    this.isLoading = true;
    const filters: any[] = [];
    const branchId = this.branchContext.getActiveBranchId();
    if (branchId !== null) {
      filters.push({ field: 'branch.id', operator: 'EQUAL', value: String(branchId) });
    }
    const filterRequest: FilterRequest = {
      filters: filters,
      sorts: [],
      page: this.pageIndex,
      size: this.pageSize,
    };
    this.salesService.getLeads(filterRequest).subscribe({
      next: (res) => {
        this.dataSource.data = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadLeads();
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  openCreateDrawer(): void {
    this.leadForm.reset({
      branchId: this.branchContext.getActiveBranchId(),
      source: 'WALK_IN'
    });
    this.isDrawerOpen = true;
  }


  closeDrawer(): void {
    this.isDrawerOpen = false;
  }

  onSubmit(): void {
    if (this.leadForm.invalid) {
      this.leadForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const request: CreateLeadRequest = this.leadForm.value;
    this.salesService.createLead(request).subscribe({
      next: () => {
        this.snackBar.open('Lead created', 'Close', { duration: 3000 });
        this.closeDrawer();
        this.loadLeads();
        this.isSubmitting = false;
      },
      error: (err) => {
        const message = err?.error?.error?.message || 'Failed to create lead';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        this.isSubmitting = false;
      },
    });
  }

  transitionStage(lead: LeadDto, newStage: LeadStage): void {
    const message = newStage === 'LOST'
      ? `Mark lead for "${lead.customerName}" as LOST?`
      : `Move lead for "${lead.customerName}" to ${newStage}?`;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Transition Lead Stage',
        message,
        confirmText: 'Confirm',
        confirmColor: newStage === 'LOST' ? 'warn' : 'primary',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.transitionLeadStage(lead.id, { newStage }).subscribe({
          next: () => {
            this.snackBar.open(`Lead moved to ${newStage}`, 'Close', { duration: 3000 });
            this.loadLeads();
          },
          error: (err) => {
            const message = err?.error?.error?.message || 'Failed to transition lead stage';
            this.snackBar.open(message, 'Close', { duration: 5000 });
          },
        });
      }
    });
  }

  deleteLead(lead: LeadDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Lead',
        message: `Delete lead for "${lead.customerName}"? This cannot be undone.`,
        confirmText: 'Delete',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.deleteLead(lead.id).subscribe({
          next: () => {
            this.snackBar.open('Lead deleted', 'Close', { duration: 3000 });
            this.loadLeads();
          },
          error: (err) => {
            const message = err?.error?.error?.message || 'Failed to delete lead';
            this.snackBar.open(message, 'Close', { duration: 5000 });
          },
        });
      }
    });
  }

  getNextStage(current: LeadStage): LeadStage | null {
    const flow: LeadStage[] = ['NEW_LEAD', 'TEST_DRIVE', 'QUOTATION', 'BOOKING', 'DELIVERY_READY', 'DELIVERED'];
    const idx = flow.indexOf(current);
    if (idx >= 0 && idx < flow.length - 1) return flow[idx + 1];
    return null;
  }

  getStageColor(stage: string): string {
    const map: Record<string, string> = {
      NEW_LEAD: '#1976d2',
      TEST_DRIVE: '#e65100',
      QUOTATION: '#f57c00',
      BOOKING: '#7b1fa2',
      DELIVERY_READY: '#388e3c',
      DELIVERED: '#2e7d32',
      LOST: '#c62828',
    };
    return map[stage] || '#666';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
