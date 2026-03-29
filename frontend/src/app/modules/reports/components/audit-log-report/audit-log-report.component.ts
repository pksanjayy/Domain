import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { ReportsService } from '../../services/reports.service';
import { AuditLogEntry } from '../../models/reports.model';

@Component({
  selector: 'app-audit-log-report',
  template: `
    <div class="report-container">
      <!-- Filters -->
      <div class="filter-bar">
        <mat-form-field appearance="outline">
          <mat-label>Module</mat-label>
          <mat-select [(value)]="moduleFilter" (selectionChange)="loadLogs()">
            <mat-option value="">All Modules</mat-option>
            <mat-option value="Vehicle">Vehicle</mat-option>
            <mat-option value="GRN">GRN</mat-option>
            <mat-option value="Lead">Lead</mat-option>
            <mat-option value="ServiceBooking">Service Booking</mat-option>
            <mat-option value="User">User</mat-option>
            <mat-option value="Role">Role</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Action</mat-label>
          <mat-select [(value)]="actionFilter" (selectionChange)="loadLogs()">
            <mat-option value="">All Actions</mat-option>
            <mat-option value="CREATE">Create</mat-option>
            <mat-option value="UPDATE">Update</mat-option>
            <mat-option value="DELETE">Delete</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>From Date</mat-label>
          <input matInput [matDatepicker]="fromPicker" [(ngModel)]="fromDate" (dateChange)="loadLogs()">
          <mat-datepicker-toggle matSuffix [for]="fromPicker"></mat-datepicker-toggle>
          <mat-datepicker #fromPicker></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>To Date</mat-label>
          <input matInput [matDatepicker]="toPicker" [(ngModel)]="toDate" (dateChange)="loadLogs()">
          <mat-datepicker-toggle matSuffix [for]="toPicker"></mat-datepicker-toggle>
          <mat-datepicker #toPicker></mat-datepicker>
        </mat-form-field>
      </div>

      <!-- Table -->
      <div class="table-card">
        <div class="table-container">
          <table mat-table [dataSource]="logs">
            <ng-container matColumnDef="performedAt">
              <th mat-header-cell *matHeaderCellDef>Timestamp</th>
              <td mat-cell *matCellDef="let row">{{ row.performedAt | date:'medium' }}</td>
            </ng-container>

            <ng-container matColumnDef="performedByUsername">
              <th mat-header-cell *matHeaderCellDef>User</th>
              <td mat-cell *matCellDef="let row">
                <div class="user-cell">
                  <div class="user-avatar-sm">{{ (row.performedByUsername || '?').charAt(0).toUpperCase() }}</div>
                  {{ row.performedByUsername || 'System' }}
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="action">
              <th mat-header-cell *matHeaderCellDef>Action</th>
              <td mat-cell *matCellDef="let row">
                <span class="action-chip" [ngClass]="row.action?.toLowerCase()">{{ row.action }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="entityName">
              <th mat-header-cell *matHeaderCellDef>Module</th>
              <td mat-cell *matCellDef="let row">{{ row.entityName }}</td>
            </ng-container>

            <ng-container matColumnDef="entityId">
              <th mat-header-cell *matHeaderCellDef>Entity ID</th>
              <td mat-cell *matCellDef="let row">#{{ row.entityId }}</td>
            </ng-container>

            <ng-container matColumnDef="ipAddress">
              <th mat-header-cell *matHeaderCellDef>IP Address</th>
              <td mat-cell *matCellDef="let row">{{ row.ipAddress || '—' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        </div>

        <mat-paginator
          [length]="totalElements"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 25, 50]"
          (page)="onPageChange($event)"
          showFirstLastButtons>
        </mat-paginator>
      </div>

      <!-- Loading -->
      <div class="loading-overlay" *ngIf="isLoading">
        <mat-spinner diameter="32"></mat-spinner>
      </div>
    </div>
  `,
  styles: [`
    .report-container { display: flex; flex-direction: column; gap: 20px; position: relative; }

    .filter-bar {
      display: flex; gap: 12px; flex-wrap: wrap;
      ::ng-deep .mat-mdc-form-field { min-width: 160px; }
    }

    .table-card {
      background: var(--dms-card); border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius); box-shadow: var(--dms-card-shadow); overflow: hidden;
    }

    .table-container {
      overflow-x: auto;
      table { width: 100%; }
      th { font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 0.03em; }
      td { font-size: 13px; }
    }

    .user-cell { display: flex; align-items: center; gap: 8px; }
    .user-avatar-sm {
      width: 28px; height: 28px; border-radius: 50%; background: var(--dms-accent);
      color: white; display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 600; flex-shrink: 0;
    }

    .action-chip {
      display: inline-block; padding: 3px 10px; border-radius: 12px;
      font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.03em;

      &.create { background: #dcfce7; color: #16a34a; }
      &.update { background: #dbeafe; color: #2563eb; }
      &.delete { background: #fee2e2; color: #dc2626; }
    }

    .loading-overlay {
      position: absolute; inset: 0; background: rgba(255,255,255,0.7);
      display: flex; align-items: center; justify-content: center; z-index: 10;
    }
  `],
})
export class AuditLogReportComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  logs: AuditLogEntry[] = [];
  displayedColumns = ['performedAt', 'performedByUsername', 'action', 'entityName', 'entityId', 'ipAddress'];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;

  moduleFilter = '';
  actionFilter = '';
  fromDate: Date | null = null;
  toDate: Date | null = null;

  constructor(private reportsService: ReportsService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.isLoading = true;
    const params: any = { page: this.pageIndex, size: this.pageSize };
    if (this.moduleFilter) params.module = this.moduleFilter;
    if (this.actionFilter) params.action = this.actionFilter;
    if (this.fromDate) params.from = this.fromDate.toISOString().split('T')[0];
    if (this.toDate) params.to = this.toDate.toISOString().split('T')[0];

    this.reportsService.getAuditLogs(params).subscribe({
      next: (res) => {
        this.logs = res.data?.content || [];
        this.totalElements = res.data?.totalElements || 0;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.logs = [];
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadLogs();
  }
}
