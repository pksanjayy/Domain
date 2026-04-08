import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SalesService } from '../../services/sales.service';
import { AdminService } from '../../../admin/services/admin.service';
import { BranchContextService } from '../../../../core/services/branch-context.service';
import { BranchDto, UserListDto } from '../../../admin/models/admin.model';
import { CreateLeadRequest } from '../../models/sales.model';

@Component({
  selector: 'app-lead-form',
  templateUrl: './lead-form.component.html',
  styleUrls: ['./lead-form.component.scss']
})
export class LeadFormComponent implements OnInit {
  leadForm!: FormGroup;
  isLoading = false;
  isEditMode = false;
  leadId: number | null = null;
  selectedLeadModel: any = null;

  branches: BranchDto[] = [];
  users: UserListDto[] = [];

  constructor(
    private fb: FormBuilder,
    private salesService: SalesService,
    private adminService: AdminService,
    private branchContext: BranchContextService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadLookupData();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.leadId = +id;
      this.loadLead();
    }
  }

  initForm(): void {
    this.leadForm = this.fb.group({
      customerId: [null, Validators.required],
      modelInterested: [''],
      branchId: [this.branchContext.getActiveBranchId(), Validators.required],
      assignedToId: [null],
      source: ['WALK_IN', Validators.required],
    });
  }

  loadLookupData(): void {
    this.adminService.getAllBranches().subscribe(res => this.branches = res.data);
    this.adminService.getUsers({ page: 0, size: 100 }).subscribe(res => this.users = res.data.content);
  }

  loadLead(): void {
    if (!this.leadId) return;
    
    this.isLoading = true;
    this.salesService.getLead(this.leadId).subscribe({
      next: (res) => {
        const lead = res.data;
        
        // Set form values immediately
        this.leadForm.patchValue({
          customerId: lead.customerId,
          assignedToId: lead.assignedToId,
          modelInterested: lead.modelInterested,
          source: lead.source,
          branchId: lead.branchId
        });
        
        // Parse modelInterested string (e.g., "Hyundai Creta") into brand and model
        if (lead.modelInterested) {
          const parts = lead.modelInterested.trim().split(/\s+/);
          if (parts.length >= 2) {
            // Use setTimeout to ensure the vehicle model selector is ready
            setTimeout(() => {
              this.selectedLeadModel = {
                brand: parts[0],
                model: parts.slice(1).join(' ')
              };
            }, 100);
          }
        }
        
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load lead', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.onCancel();
      }
    });
  }

  onModelSelected(model: any): void {
    if (model && model.brand && model.model) {
      this.leadForm.patchValue({
        modelInterested: `${model.brand} ${model.model}`
      });
    }
  }

  onSubmit(): void {
    if (this.leadForm.invalid) {
      this.leadForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const request: CreateLeadRequest = this.leadForm.value;

    if (this.isEditMode && this.leadId) {
      this.salesService.updateLead(this.leadId, request).subscribe({
        next: () => {
          this.snackBar.open('Lead updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/leads']);
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to update lead';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    } else {
      this.salesService.createLead(request).subscribe({
        next: () => {
          this.snackBar.open('Lead created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/sales/leads']);
        },
        error: (err) => {
          const message = err?.error?.error?.message || 'Failed to create lead';
          this.snackBar.open(message, 'Close', { duration: 5000 });
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/sales/leads']);
  }
}
