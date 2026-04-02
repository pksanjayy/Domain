import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SalesService } from '../../services/sales.service';
import { LeadDto, LeadStage } from '../../models/sales.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-lead-detail',
  templateUrl: './lead-detail.component.html',
  styleUrls: ['./lead-detail.component.scss']
})
export class LeadDetailComponent implements OnInit {
  leadId!: number;
  lead: LeadDto | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private salesService: SalesService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.leadId = +id;
      this.loadLead();
    }
  }

  loadLead(): void {
    this.isLoading = true;
    this.salesService.getLead(this.leadId).subscribe({
      next: (res) => {
        this.lead = res.data;
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load lead details', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.goBack();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/sales/leads']);
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

  transitionStage(newStage: LeadStage): void {
    if (!this.lead) return;
    
    const isLost = newStage === 'LOST';
    const message = isLost
      ? `Mark lead for "${this.lead.customerName}" as LOST?`
      : `Move lead for "${this.lead.customerName}" to ${newStage.replace('_', ' ')}?`;

    // To properly support LOST reason, you would ideally show a form dialog instead of a simple ConfirmDialog.
    // For now, we will pass a default lost reason string if it is LOST. 
    // Wait, the backend requires a lost reason when marked as LOST. We can reuse a simple dialog or prompt.
    // Let's just pass "Not interested" as default if we don't build a custom UI for it in this PR.
    
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Transition Lead Stage',
        message: message + (isLost ? ' (Lost reason will be set to: Status update)' : ''),
        confirmText: 'Confirm',
        confirmColor: isLost ? 'warn' : 'primary',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.salesService.transitionLeadStage(this.lead!.id, { 
          newStage, 
          lostReason: isLost ? 'Status update' : undefined 
        }).subscribe({
          next: () => {
            this.snackBar.open(`Lead moved to ${newStage}`, 'Close', { duration: 3000 });
            this.loadLead(); // reload
          },
          error: (err) => {
            const errorMsg = err?.error?.error?.message || 'Failed to transition lead stage';
            this.snackBar.open(errorMsg, 'Close', { duration: 5000 });
          },
        });
      }
    });
  }
}
