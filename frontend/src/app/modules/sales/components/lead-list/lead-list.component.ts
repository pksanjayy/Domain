import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
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

  filterValues: any = {
    globalSearch: '',
    stage: '',
    source: ''
  };

  isDrawerOpen = false;
  leadForm!: FormGroup;
  isSubmitting = false;
  editingLeadId: number | null = null;

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
    private router: Router
  ) {}


  ngOnInit(): void {
    this.initForm();
    this.loadLeads();
    this.loadLookupData();
    this.dataSource.filterPredicate = this.createFilter();

    // Reload when branch changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadLeads());
  }

  createFilter(): (data: LeadDto, filter: string) => boolean {
    return (data: LeadDto, filter: string): boolean => {
      const searchTerms = JSON.parse(filter);
      const matchSearch = !!(!searchTerms.globalSearch ||
        data.customerName?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.customerMobile?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.modelInterested?.toLowerCase().includes(searchTerms.globalSearch) ||
        data.assignedToUsername?.toLowerCase().includes(searchTerms.globalSearch));
      const matchStage = !!(!searchTerms.stage || data.stage === searchTerms.stage);
      const matchSource = !!(!searchTerms.source || data.source === searchTerms.source);
      return matchSearch && matchStage && matchSource;
    };
  }

  loadLookupData(): void {
    this.adminService.getAllBranches().subscribe(res => this.branches = res.data);
    // Loading users (could be optimized to only sales users later)
    this.adminService.getUsers({ page: 0, size: 100 }).subscribe(res => this.users = res.data.content);
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
    this.filterValues.globalSearch = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  onStageFilterChange(stage: string): void {
    this.filterValues.stage = stage;
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  onSourceFilterChange(source: string): void {
    this.filterValues.source = source;
    this.dataSource.filter = JSON.stringify(this.filterValues);
  }

  openCreateDrawer(): void {
    this.editingLeadId = null;
    this.leadForm.reset({
      branchId: this.branchContext.getActiveBranchId(),
      source: 'WALK_IN'
    });
    this.isDrawerOpen = true;
  }

  editLead(lead: LeadDto): void {
    this.editingLeadId = lead.id;
    this.leadForm.patchValue({
      customerId: lead.customerId,
      assignedToId: lead.assignedToId,
      modelInterested: lead.modelInterested,
      source: lead.source,
      branchId: lead.branchId
    });
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
    this.editingLeadId = null;
  }

  onSubmit(): void {
    if (this.leadForm.invalid) {
      this.leadForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const request: CreateLeadRequest = this.leadForm.value;

    if (this.editingLeadId) {
      this.salesService.updateLead(this.editingLeadId, request).subscribe({
        next: () => {
          this.snackBar.open('Lead updated', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadLeads();
          this.isSubmitting = false;
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to update lead';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isSubmitting = false;
        },
      });
    } else {
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
  }

  onRowClick(lead: LeadDto): void {
    this.router.navigate(['/sales/leads', lead.id]);
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
